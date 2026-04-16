package com.uth.mobileBE.services;

import com.uth.mobileBE.Utils.LoanSpecification;
import com.uth.mobileBE.dto.request.LoanRequest;
import com.uth.mobileBE.dto.response.LoanDetailResponse;
import com.uth.mobileBE.dto.response.LoanResponse;
import com.uth.mobileBE.models.Loan;
import com.uth.mobileBE.models.LoanDetail;
import com.uth.mobileBE.models.enums.StatusLoan;
import com.uth.mobileBE.models.enums.StatusLoanDetail;
import com.uth.mobileBE.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    @Autowired private LoanRepository loanRepository;
    @Autowired private LibraryRepository libraryRepository;
    @Autowired private ReaderRepository readerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private LoanDetailRepository loanDetailRepository;

    @Transactional(readOnly = true)
    // Hàm lấy danh sách theo id thư viện có kèm theo bộ lọc
    public List<LoanResponse> getLoansWithFilter(
            Long libraryId,
            String statusFilter,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String searchQuery) {

        Specification<Loan> spec = LoanSpecification.filterLoans(
                libraryId, statusFilter, fromDate, toDate, searchQuery
        );

        return loanRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn gốc với ID: " + id));

        // Nhờ @Transactional, loan.getLoanDetails() sẽ tự động fetch data mà không bị lỗi Lazy
        if (loan.getLoanDetails() == null || loan.getLoanDetails().isEmpty()) {
            System.out.println("LOG: Phiếu mượn " + id + " tồn tại nhưng bảng loan_detail lại trống!");
        }

        return mapToResponse(loan);
    }

    @Transactional
    public LoanResponse createLoan(LoanRequest request) {
        var library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Thư viện không tồn tại"));
        var reader = readerRepository.findById(request.getReaderId())
                .orElseThrow(() -> new RuntimeException("Người đọc không tồn tại"));
        var user = userRepository.findById(request.getProcessedBy())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        Loan loan = Loan.builder()
                .library(library)
                .reader(reader)
                .processedBy(user)
                .borrowDate(LocalDateTime.now())
                // Tạo mới mặc định luôn là ACTIVE, không cần request.getStatus() nữa
                .status(StatusLoan.ACTIVE)
                .build();

        return mapToResponse(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse updateLoan(Long id, LoanRequest request) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ID: " + id));

        if (request.getLibraryId() != null) {
            loan.setLibrary(libraryRepository.findById(request.getLibraryId())
                    .orElseThrow(() -> new RuntimeException("Thư viện không tồn tại")));
        }

        if (request.getReaderId() != null) {
            loan.setReader(readerRepository.findById(request.getReaderId())
                    .orElseThrow(() -> new RuntimeException("Người đọc không tồn tại")));
        }

        if (request.getProcessedBy() != null) {
            loan.setProcessedBy(userRepository.findById(request.getProcessedBy())
                    .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại")));
        }

        return mapToResponse(loanRepository.save(loan));
    }

    @Transactional
    public void deleteLoan(Long id) {
        loanRepository.deleteById(id);
    }

    // Đổi tên từ countBorrowingLoans thành countActiveLoans cho đúng chuẩn enum mới
    @Transactional(readOnly = true)
    public Long countActiveLoans(Long libraryId) {
        return loanRepository.countByLibrary_LibraryIdAndStatus(libraryId, StatusLoan.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Long countOverdueLoans(Long libraryId) {
        return loanRepository.countByLibrary_LibraryIdAndStatus(libraryId, StatusLoan.OVERDUE);
    }

    // --- HÀM MAPPER CHÍNH ---
    private LoanResponse mapToResponse(Loan loan) {
        StatusLoan finalStatus = loan.getStatus();

        // 1. Logic tính toán trạng thái ưu tiên theo thời gian thực (Giống Specification)
        if (finalStatus == StatusLoan.ACTIVE && loan.getLoanDetails() != null) {
            LocalDateTime now = LocalDateTime.now();

            boolean hasViolated = loan.getLoanDetails().stream()
                    .anyMatch(d -> d.getStatus() == StatusLoanDetail.LOST || d.getStatus() == StatusLoanDetail.DAMAGED);

            boolean hasOverdue = loan.getLoanDetails().stream()
                    .anyMatch(d -> d.getStatus() == StatusLoanDetail.OVERDUE ||
                            (d.getStatus() == StatusLoanDetail.BORROWING && d.getDueDate() != null && d.getDueDate().isBefore(now)));

            // Ưu tiên trạng thái: VIOLATED > OVERDUE > ACTIVE
            if (hasViolated) {
                finalStatus = StatusLoan.VIOLATED;
            } else if (hasOverdue) {
                finalStatus = StatusLoan.OVERDUE;
            }
        }

        // 2. Chuyển đổi LoanDetail thành LoanDetailResponse
        List<LoanDetailResponse> detailDtos = loan.getLoanDetails() != null ?
                loan.getLoanDetails().stream().map(detail -> {
                    var book = detail.getBookCopy().getBook();

                    return LoanDetailResponse.builder()
                            .loanDetailId(detail.getLoanDetailId())
                            .loanId(loan.getLoanId())
                            .copyId(detail.getBookCopy().getCopyId())
                            .bookId(book.getBookId()) // Giả sử model Book của bạn có getBookId()
                            .bookTitle(book.getTitle())

                            // ĐƯA VÀO ĐÂY MỚI ĐÚNG VỊ TRÍ
                            .author(book.getAuthor())
                            .category(book.getCategory() != null ? book.getCategory().getName() : "Chưa cập nhật")

                            .dueDate(detail.getDueDate())
                            .returnDate(detail.getReturnDate())
                            .status(detail.getStatus())
                            // .penaltyAmount(detail.getPenaltyAmount()) // Bỏ comment nếu model có trường này
                            .createdAt(detail.getCreatedAt())
                            .updateAt(detail.getUpdateAt())
                            .build();
                }).collect(Collectors.toList()) : List.of();

        // 3. Build LoanResponse
        return LoanResponse.builder()
                .loanId(loan.getLoanId())
                .libraryName(loan.getLibrary().getName())
                .readerName(loan.getReader().getFullName())
                .processorName(loan.getProcessedBy().getFullname())
                .borrowDate(loan.getBorrowDate())
                .status(finalStatus) // Trả về status đã được update theo thời gian thực
                .createdAt(loan.getCreatedAt())
                .updateAt(loan.getUpdateAt())
                .loanDetails(detailDtos) // Truyền List<LoanDetailResponse> vào
                .build();
    }
}