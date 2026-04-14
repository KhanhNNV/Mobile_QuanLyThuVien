package com.uth.mobileBE.services;

import com.uth.mobileBE.Utils.LoanSpecification;
import com.uth.mobileBE.dto.request.LoanRequest;
import com.uth.mobileBE.dto.response.LoanResponse;
import com.uth.mobileBE.models.Loan;
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


    @Transactional(readOnly = true) // Thêm dòng này
    //Hàm lấy danh sách theo id thư viện có kèm theo bộ lọc
    public List<LoanResponse> getLoansWithFilter(
            Long libraryId,
            String statusFilter,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String searchQuery) {

        // Gọi Specification để tự động sinh ra câu lệnh SQL có điều kiện
        Specification<Loan> spec = LoanSpecification.filterLoans(
                libraryId, statusFilter, fromDate, toDate, searchQuery
        );

        // Lấy danh sách từ DB và map sang Response
        return loanRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }




    public LoanResponse getLoanById(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn ID: " + id));
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
                .borrowDate(LocalDateTime.now()) // Dùng LocalDateTime trực tiếp
                .status(request.getStatus())
                .build();

        return mapToResponse(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse updateLoan(Long id, LoanRequest request) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ID: " + id));

        // Partial Update: Chỉ cập nhật nếu request có gửi dữ liệu
        if (request.getStatus() != null) loan.setStatus(request.getStatus());

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

    public void deleteLoan(Long id) {
        loanRepository.deleteById(id);
    }


    public Long countBorrowingLoans(Long libraryId) {
        return loanRepository.countByLibrary_LibraryIdAndStatus(libraryId, StatusLoan.BORROWING);
    }

    public Long countOverdueLoans(Long libraryId) {
        return loanRepository.countByLibrary_LibraryIdAndStatus(libraryId, StatusLoan.OVERDUE);
    }

    private LoanResponse mapToResponse(Loan loan) {
        // 1. Lấy trạng thái gốc từ Database
        StatusLoan finalStatus = loan.getStatus();

        // 2. LOGIC TÍNH TOÁN OVERDUE (Trễ hạn)
        // Nếu phiếu đang mượn, ta phải kiểm tra xem có cuốn sách nào bị lố ngày không
        if (finalStatus == StatusLoan.BORROWING && loan.getLoanDetails() != null) {
            LocalDateTime now = LocalDateTime.now();

            // Quét qua danh sách chi tiết mượn
            boolean isOverdue = loan.getLoanDetails().stream()
                    .anyMatch(detail ->
                            // Sách chưa trả VÀ ngày hết hạn nhỏ hơn thời gian hiện tại
                            detail.getStatus() == StatusLoanDetail.BORROWING &&
                                    detail.getDueDate() != null &&
                                    detail.getDueDate().isBefore(now)
                    );

            // Nếu phát hiện có sách trễ hạn, ghi đè trạng thái thành OVERDUE để gửi cho Mobile
            if (isOverdue) {
                finalStatus = StatusLoan.OVERDUE;
            }
        }

        // 3. Build Response gửi về Mobile
        return LoanResponse.builder()
                .loanId(loan.getLoanId())
                .libraryName(loan.getLibrary().getName())
                .readerName(loan.getReader().getFullName())
                .processorName(loan.getProcessedBy().getFullname())
                .borrowDate(loan.getBorrowDate())
                .status(finalStatus) // Gửi trạng thái ĐÃ ĐƯỢC TÍNH TOÁN LẠI
                .bookTitles(loan.getLoanDetails() != null ?
                        loan.getLoanDetails().stream()
                                .map(d -> d.getBookCopy().getBook().getTitle())
                                .collect(Collectors.toList()) : List.of())
                .createdAt(loan.getCreatedAt())
                .updateAt(loan.getUpdateAt())
                .build();
    }
}