package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.LoanDetailRequest;
import com.uth.mobileBE.dto.request.UpdateLoanDetailRequest;
import com.uth.mobileBE.dto.request.ViolationRequest;
import com.uth.mobileBE.dto.response.LoanDetailResponse;
import com.uth.mobileBE.models.BookCopy;
import com.uth.mobileBE.models.Loan;
import com.uth.mobileBE.models.LoanDetail;
import com.uth.mobileBE.models.enums.*;
import com.uth.mobileBE.repositories.BookCopyRepository;
import com.uth.mobileBE.repositories.LoanDetailRepository;
import com.uth.mobileBE.repositories.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanDetailService {

    private final LoanDetailRepository loanDetailRepository;
    private final LoanRepository loanRepository;
    private final BookCopyRepository bookCopyRepository;
    private final ViolationService violationService;

    public List<LoanDetailResponse> getAllDetails() {
        return loanDetailRepository.findAll().stream()
                .map(detail -> {
                    evaluateAndApplyOverdue(detail);
                    return mapToResponse(detail);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanDetailResponse updateDetailAdmin(Long loanDetailId, UpdateLoanDetailRequest request) {
        LoanDetail detail = loanDetailRepository.findById(loanDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết phiếu mượn"));

        Long oldCopyId = detail.getBookCopy().getCopyId();
        Long newCopyId = request.getCopyId();

        // 1. NẾU ADMIN ĐỔI CUỐN SÁCH KHÁC
        if (!oldCopyId.equals(newCopyId)) {
            BookCopy newCopy = bookCopyRepository.findById(newCopyId)
                    .orElseThrow(() -> new RuntimeException("Sách thay thế không tồn tại"));

            // Giải phóng sách cũ
            BookCopy oldCopy = detail.getBookCopy();
            oldCopy.setStatus(StatusBookCopy.AVAILABLE);
            bookCopyRepository.save(oldCopy);

            // Gắn sách mới
            detail.setBookCopy(newCopy);
            if ("BORROWING".equals(request.getStatus())) {
                newCopy.setStatus(StatusBookCopy.BORROWED);
            }
            bookCopyRepository.save(newCopy);
        }

        // 2. CẬP NHẬT TRẠNG THÁI & LOGIC NGÀY TRẢ / TÌNH TRẠNG SÁCH
        StatusLoanDetail newStatus = StatusLoanDetail.valueOf(request.getStatus());
        detail.setStatus(newStatus);
        BookCopy currentCopy = detail.getBookCopy();
        // --- LOGIC TỰ ĐỘNG CẬP NHẬT CONDITION ---
        if (newStatus == StatusLoanDetail.DAMAGED && request.getCondition() != null) {
            // Nếu là Hư hỏng: Lấy giá trị FAIR/POOR từ App gửi lên
            currentCopy.setCondition(ConditionBookCopy.valueOf(request.getCondition()));
        } else if (newStatus != StatusLoanDetail.DAMAGED) {
            // Nếu là Trả bình thường hoặc Đang mượn:
            // Nếu tình trạng cũ đang là xấu (FAIR/POOR) thì reset về GOOD (đã sửa/vệ sinh)
            if (currentCopy.getCondition() == ConditionBookCopy.FAIR ||
                    currentCopy.getCondition() == ConditionBookCopy.POOR) {
                currentCopy.setCondition(ConditionBookCopy.GOOD);
            }
        }

        // Case 1: Làm mất sách
        if (newStatus == StatusLoanDetail.LOST) {
            currentCopy.setStatus(StatusBookCopy.LOST);
            detail.setReturnDate(null); // Mất sách thì không có ngày trả thực tế
            createViolationForDetail(detail, "Admin ghi nhận: Khách làm mất sách");
        }
        // Case 2: Trả sách (bao gồm cả trả sách nhưng bị hỏng)
        else if (newStatus == StatusLoanDetail.RETURNED || newStatus == StatusLoanDetail.DAMAGED) {
            detail.setReturnDate(LocalDateTime.now()); // Đã trả/Hỏng đều phải có ngày trả thực tế

            // Cập nhật trạng thái sách trong kho
            if (newStatus == StatusLoanDetail.DAMAGED) {
                currentCopy.setStatus(StatusBookCopy.DAMAGED);
                createViolationForDetail(detail, "Admin ghi nhận: Sách bị hư hỏng khi trả");
            } else {
                currentCopy.setStatus(StatusBookCopy.AVAILABLE);
            }
        }
        // Case 3: Chỉnh về đang mượn (Gia hạn/Sửa nhầm)
        else if (newStatus == StatusLoanDetail.BORROWING) {
            detail.setReturnDate(null);
            currentCopy.setStatus(StatusBookCopy.BORROWED);
        }

        // Lưu trạng thái sách
        bookCopyRepository.save(currentCopy);

        // 3. CẬP NHẬT HẠN TRẢ (GIA HẠN)
        if (request.getDueDate() != null) {
            detail.setDueDate(request.getDueDate());
        }

        // Lưu chi tiết phiếu và đồng bộ trạng thái phiếu tổng
        loanDetailRepository.save(detail);
        syncLoanStatus(detail.getLoan().getLoanId());

        return mapToResponse(detail);
    }

    @Transactional
    public LoanDetailResponse createDetail(LoanDetailRequest request) {
        var loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new RuntimeException("Phiếu mượn không tồn tại"));
        var copy = bookCopyRepository.findById(request.getCopyId())
                .orElseThrow(() -> new RuntimeException("Bản sao sách không tồn tại"));

        // SỬA LỖI: Tự động tính ngày hết hạn (dueDate) dựa trên số ngày mượn (borrowDays) từ request
        LocalDateTime calculatedDueDate = LocalDateTime.now().plusDays(request.getBorrowDays());

        LoanDetail detail = LoanDetail.builder()
                .loan(loan)
                .bookCopy(copy)
                .dueDate(calculatedDueDate) // Sử dụng biến vừa tính
                .status(StatusLoanDetail.BORROWING)
                // Đã xóa .penaltyAmount() vì entity không có thuộc tính này
                .build();

        copy.setStatus(StatusBookCopy.BORROWED);
        bookCopyRepository.save(copy);

        return mapToResponse(loanDetailRepository.save(detail));
    }

    @Transactional
    public LoanDetailResponse returnBook(Long loanDetailId, ConditionBookCopy returnCondition) {
        LoanDetail detail = loanDetailRepository.findById(loanDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết phiếu mượn"));

        // Nếu sách ĐÃ BỊ ĐÁNH DẤU LÀ MẤT (LOST) từ trước, không cho phép thao tác trả sách nữa.
        if (detail.getStatus() == StatusLoanDetail.LOST) {
            throw new RuntimeException("Sách này đã bị khóa ở trạng thái MẤT, không thể thực hiện trả sách. Vui lòng liên hệ quản trị viên.");
        }

        // Quét quá hạn (chỉ cập nhật thành OVERDUE hoặc LOST nếu hôm nay mới quét trúng mốc 15 ngày)
        evaluateAndApplyOverdue(detail);

        // BỔ SUNG QUAN TRỌNG:
        // Sau khi quét, nếu hệ thống vừa phát hiện nó trễ quá 15 ngày và ĐÃ tự động chuyển nó thành LOST,
        // thì ta phải NGỪNG hàm lại ngay lập tức (return luôn kết quả), không chạy tiếp logic trả sách bên dưới.
        if (detail.getStatus() == StatusLoanDetail.LOST) {
            return mapToResponse(detail);
        }

        // ==========================================
        // Nếu sách còn cứu được (chỉ OVERDUE hoặc BORROWING), thì mới xử lý logic trả:
        // ==========================================

        detail.setReturnDate(LocalDateTime.now());
        BookCopy copy = detail.getBookCopy();

        // Cập nhật tình trạng sách vật lý từ giao diện App
        if (returnCondition != null) {
            copy.setCondition(returnCondition);
        }

        // Xử lý logic trạng thái
        if (returnCondition == ConditionBookCopy.FAIR || returnCondition == ConditionBookCopy.POOR) {
            detail.setStatus(StatusLoanDetail.DAMAGED);
            copy.setStatus(StatusBookCopy.DAMAGED);

            // Tự động báo cáo vi phạm
            createViolationForDetail(detail, "Sách bị hư hỏng khi trả (Tình trạng: " + returnCondition + ")");
        } else {
            // Nếu tình trạng sách bình thường (NEW, GOOD)
            detail.setStatus(StatusLoanDetail.RETURNED);
            copy.setStatus(StatusBookCopy.AVAILABLE);
        }

        bookCopyRepository.save(copy);
        loanDetailRepository.save(detail);

        // Đồng bộ lại Phiếu mượn gốc
        syncLoanStatus(detail.getLoan().getLoanId());

        return mapToResponse(detail);
    }
    private void evaluateAndApplyOverdue(LoanDetail detail) {
        if (detail.getStatus() == StatusLoanDetail.BORROWING && detail.getDueDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(detail.getDueDate())) {
                long daysOverdue = ChronoUnit.DAYS.between(detail.getDueDate(), now);

                if (daysOverdue > 15) {
                    detail.setStatus(StatusLoanDetail.LOST);

                    BookCopy copy = detail.getBookCopy();
                    copy.setStatus(StatusBookCopy.LOST);
                    bookCopyRepository.save(copy);

                    createViolationForDetail(detail, "Sách trễ hạn quá 15 ngày, hệ thống tự động đánh dấu LOST");
                    syncLoanStatus(detail.getLoan().getLoanId());
                } else {
                    detail.setStatus(StatusLoanDetail.OVERDUE);
                }
                loanDetailRepository.save(detail);
            }
        }
    }

    private void createViolationForDetail(LoanDetail detail, String reason) {
        ViolationRequest vReq = ViolationRequest.builder()
                .readerId(detail.getLoan().getReader().getReaderId())
                .libraryId(detail.getLoan().getLibrary().getLibraryId())
                .loanId(detail.getLoan().getLoanId())
                .reason(reason)
                .status("ACTIVE")
                .build();

        violationService.createViolation(vReq);
    }

    private void syncLoanStatus(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElse(null);
        if (loan != null) {
            List<LoanDetail> allDetails = loanDetailRepository.findByLoan_LoanId(loanId);

            boolean hasBorrowing = false;
            boolean hasOverdue = false;
            boolean hasViolated = false;

            for (LoanDetail d : allDetails) {
                if (d.getStatus() == StatusLoanDetail.BORROWING) hasBorrowing = true;
                if (d.getStatus() == StatusLoanDetail.OVERDUE) hasOverdue = true;
                if (d.getStatus() == StatusLoanDetail.LOST || d.getStatus() == StatusLoanDetail.DAMAGED) hasViolated = true;
            }

            if (hasViolated) {
                loan.setStatus(StatusLoan.VIOLATED);
            } else if (hasOverdue) {
                loan.setStatus(StatusLoan.OVERDUE);
            } else if (hasBorrowing) {
                loan.setStatus(StatusLoan.ACTIVE);
            } else {
                loan.setStatus(StatusLoan.COMPLETED);
            }

            loanRepository.save(loan);
        }
    }

    public void deleteDetail(Long loanDetailId) {
        loanDetailRepository.deleteById(loanDetailId);
    }

    public List<String> getDueTodayAlerts(Long libraryId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<LoanDetail> dueDetails = loanDetailRepository.findDueToday(libraryId, startOfDay, endOfDay);

        return dueDetails.stream()
                .map(ld -> "Phiếu mượn #" + ld.getLoan().getLoanId() +
                        " (Sách: " + ld.getBookCopy().getBook().getTitle() +
                        ") đến hạn trả ngay hôm nay!")
                .collect(Collectors.toList());
    }

    private LoanDetailResponse mapToResponse(LoanDetail detail) {
        return LoanDetailResponse.builder()
                .loanDetailId(detail.getLoanDetailId())
                .loanId(detail.getLoan().getLoanId())
                .copyId(detail.getBookCopy().getCopyId())
                .bookTitle(detail.getBookCopy().getBook().getTitle())
                .dueDate(detail.getDueDate())
                .returnDate(detail.getReturnDate())
                .status(detail.getStatus())
                .createdAt(detail.getCreatedAt())
                .updateAt(detail.getUpdateAt())
                .build();
    }
}