package com.uth.mobileBE.services;

import com.uth.mobileBE.Utils.LoanSpecification;
import com.uth.mobileBE.dto.request.LoanRequest;
import com.uth.mobileBE.dto.response.BookDetailInfoDto;
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



    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long id) {
        // 1. Tìm phiếu mượn gốc trước
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn gốc với ID: " + id));

        // 2. Lấy danh sách chi tiết
        List<LoanDetail> details = loanDetailRepository.findByLoan_LoanId(id);

        // Nếu danh sách rỗng, đừng throw Exception vội, hãy log ra để xem
        if (details.isEmpty()) {
            System.out.println("LOG: Phiếu mượn " + id + " tồn tại nhưng bảng loan_detail lại trống!");
            // Bạn nên trả về một List rỗng trong DTO thay vì sập app
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
        StatusLoan finalStatus = loan.getStatus();

        // 1. Logic tính toán trạng thái Overdue (Giữ nguyên của bạn)
        if (finalStatus == StatusLoan.BORROWING && loan.getLoanDetails() != null) {
            LocalDateTime now = LocalDateTime.now();
            boolean isOverdue = loan.getLoanDetails().stream()
                    .anyMatch(detail ->
                            detail.getStatus() == StatusLoanDetail.BORROWING &&
                                    detail.getDueDate() != null &&
                                    detail.getDueDate().isBefore(now)
                    );
            if (isOverdue) {
                finalStatus = StatusLoan.OVERDUE;
            }
        }

        // 2. Chuyển đổi LoanDetail thành BookDetailInfoDto
        List<BookDetailInfoDto> detailDtos = loan.getLoanDetails() != null ?
                loan.getLoanDetails().stream().map(detail -> {
                    // Lấy đối tượng Book ra để code ngắn gọn hơn
                    var book = detail.getBookCopy().getBook();

                    return BookDetailInfoDto.builder()
                            .copyId(detail.getBookCopy().getCopyId()) // Cần thiết cho Android
                            .title(book.getTitle())
                            .author(book.getAuthor())
                            // Giả sử Book có quan hệ với Category, nếu không có bạn có thể bỏ qua
                            .category(book.getCategory() != null ? book.getCategory().getName() : "N/A")
                            .dueDate(detail.getDueDate())
                            .returnDate(detail.getReturnDate())
                            .status(detail.getStatus())
                            .build();
                }).collect(Collectors.toList()) : List.of();

        // 3. Build LoanResponse
        return LoanResponse.builder()
                .loanId(loan.getLoanId())
                .libraryName(loan.getLibrary().getName())
                .readerName(loan.getReader().getFullName())
                .processorName(loan.getProcessedBy().getFullname())
                .borrowDate(loan.getBorrowDate())
                .status(finalStatus)
                .createdAt(loan.getCreatedAt())
                .updateAt(loan.getUpdateAt())
                .bookDetails(detailDtos) // Truyền danh sách DTO mới vào đây
                .build();
    }
}