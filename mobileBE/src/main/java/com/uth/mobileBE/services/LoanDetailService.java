package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.LoanDetailRequest;
import com.uth.mobileBE.dto.response.LoanDetailResponse;
import com.uth.mobileBE.models.BookCopy;
import com.uth.mobileBE.models.Loan;
import com.uth.mobileBE.models.LoanDetail;
import com.uth.mobileBE.models.LoanDetailId;
import com.uth.mobileBE.models.enums.StatusBookCopy;
import com.uth.mobileBE.models.enums.StatusLoan;
import com.uth.mobileBE.models.enums.StatusLoanDetail;
import com.uth.mobileBE.repositories.BookCopyRepository;
import com.uth.mobileBE.repositories.LoanDetailRepository;
import com.uth.mobileBE.repositories.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanDetailService {

    @Autowired private LoanDetailRepository loanDetailRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private BookCopyRepository bookCopyRepository;

    public List<LoanDetailResponse> getAllDetails() {
        return loanDetailRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanDetailResponse createDetail(LoanDetailRequest request) {
        LoanDetailId id = new LoanDetailId(request.getLoanId(), request.getCopyId());

        var loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new RuntimeException("Phiếu mượn không tồn tại"));
        var copy = bookCopyRepository.findById(request.getCopyId())
                .orElseThrow(() -> new RuntimeException("Bản sao sách không tồn tại"));

        LoanDetail detail = LoanDetail.builder()
                .id(id)
                .loan(loan)
                .bookCopy(copy)
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .penaltyAmount(0.0)
                .build();

        return mapToResponse(loanDetailRepository.save(detail));
    }

    @Transactional
    public LoanDetailResponse updateDetail(Long loanId, Long oldCopyId, LoanDetailRequest request) {
        // 1. Lấy chi tiết sách cũ đang tồn tại trong phiếu
        LoanDetail oldDetail = loanDetailRepository.findById(new LoanDetailId(loanId, oldCopyId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết phiếu mượn"));

        Long newCopyId = request.getCopyId();
        LoanDetail finalDetail;

        // ==============================================================
        // TRƯỜNG HỢP 1: THAY ĐỔI QUYỂN SÁCH (copyId mới khác oldCopyId)
        // ==============================================================
        if (newCopyId != null && !newCopyId.equals(oldCopyId)) {
            // a. Lấy thông tin sách mới từ DB
            BookCopy newBookCopy = bookCopyRepository.findById(newCopyId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy quyển sách mới"));

            // b. Giải phóng quyển sách cũ -> AVAILABLE
            BookCopy oldBookCopy = oldDetail.getBookCopy();
            oldBookCopy.setStatus(StatusBookCopy.AVAILABLE);
            bookCopyRepository.save(oldBookCopy);

            // c. Xóa dòng chi tiết cũ (vì khóa chính thay đổi)
            loanDetailRepository.delete(oldDetail);
            loanDetailRepository.flush(); // Ép xóa ngay lập tức để tránh trùng lặp

            // d. Tạo dòng chi tiết mới
            LoanDetail newDetail = new LoanDetail();
            newDetail.setId(new LoanDetailId(loanId, newCopyId));
            newDetail.setLoan(oldDetail.getLoan());
            newDetail.setBookCopy(newBookCopy);
            newDetail.setDueDate(request.getDueDate() != null ? request.getDueDate() : oldDetail.getDueDate());
            newDetail.setStatus(request.getStatus() != null ? request.getStatus() : oldDetail.getStatus());

            // Cập nhật ngày trả dựa trên trạng thái mới
            handleReturnDate(newDetail, request.getStatus());

            finalDetail = loanDetailRepository.save(newDetail);

            // e. Đánh dấu quyển sách mới là đã bị mượn (nếu trạng thái là BORROWING)
            if (newDetail.getStatus() == StatusLoanDetail.BORROWING) {
                newBookCopy.setStatus(StatusBookCopy.BORROWED);
                bookCopyRepository.save(newBookCopy);
            }
        }
        // ==============================================================
        // TRƯỜNG HỢP 2: GIỮ NGUYÊN SÁCH, CHỈ ĐỔI NGÀY HOẶC TRẠNG THÁI
        // ==============================================================
        else {
            if (request.getDueDate() != null) {
                oldDetail.setDueDate(request.getDueDate());
            }
            if (request.getStatus() != null) {
                oldDetail.setStatus(request.getStatus());
                handleReturnDate(oldDetail, request.getStatus());

                // Nếu đổi từ mất/hỏng về lại đang mượn/đã trả thì cập nhật lại bảng book_copy
                updateBookCopyStatus(oldDetail.getBookCopy(), request.getStatus());
            }
            finalDetail = loanDetailRepository.save(oldDetail);
        }

        // 4. LOGIC ĐỒNG BỘ PHIẾU MƯỢN GỐC (Giữ nguyên logic của bạn)
        syncLoanStatus(loanId);

        return mapToResponse(finalDetail);
    }

    // Hàm hỗ trợ xử lý ngày trả
    private void handleReturnDate(LoanDetail detail, StatusLoanDetail status) {
        if (status == StatusLoanDetail.BORROWING) {
            detail.setReturnDate(null);
        } else if (status != null) {
            detail.setReturnDate(LocalDateTime.now());
        }
    }

    // Hàm cập nhật trạng thái sách vật lý
    private void updateBookCopyStatus(BookCopy copy, StatusLoanDetail status) {
        if (status == StatusLoanDetail.BORROWING) {
            copy.setStatus(StatusBookCopy.BORROWED);
        } else if (status == StatusLoanDetail.LOST) {
            copy.setStatus(StatusBookCopy.LOST);
        } else if (status == StatusLoanDetail.DAMAGED) {
            copy.setStatus(StatusBookCopy.DAMAGED);
        } else {
            copy.setStatus(StatusBookCopy.AVAILABLE);
        }
        bookCopyRepository.save(copy);
    }

    // Hàm đồng bộ trạng thái phiếu mượn
    private void syncLoanStatus(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElse(null);
        if (loan != null) {
            List<LoanDetail> allDetails = loanDetailRepository.findByLoan_LoanId(loanId);
            boolean hasBorrowingBook = allDetails.stream()
                    .anyMatch(d -> d.getStatus() == StatusLoanDetail.BORROWING);
            loan.setStatus(hasBorrowingBook ? StatusLoan.BORROWING : StatusLoan.RETURNED);
            loanRepository.save(loan);
        }
    }

    public void deleteDetail(Long loanId, Long copyId) {
        loanDetailRepository.deleteById(new LoanDetailId(loanId, copyId));
    }


    public List<String> getDueTodayAlerts(Long libraryId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay(); // 00:00:00 hôm nay
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX); // 23:59:59 hôm nay

        List<LoanDetail> dueDetails = loanDetailRepository.findDueToday(libraryId, startOfDay, endOfDay);

        return dueDetails.stream()
                .map(ld -> "Phiếu mượn #" + ld.getLoan().getLoanId() +
                        " (Sách: " + ld.getBookCopy().getBook().getTitle() +
                        ") đến hạn trả ngay hôm nay!")
                .collect(Collectors.toList());
    }

    private LoanDetailResponse mapToResponse(LoanDetail detail) {
        return LoanDetailResponse.builder()
                .loanId(detail.getId().getLoanId())
                .copyId(detail.getId().getCopyId())
                .bookTitle(detail.getBookCopy().getBook().getTitle())
                .dueDate(detail.getDueDate())
                .returnDate(detail.getReturnDate())
                .status(detail.getStatus())
                .penaltyAmount(detail.getPenaltyAmount())
                .createdAt(detail.getCreatedAt())
                .updateAt(detail.getUpdateAt())
                .build();
    }
}