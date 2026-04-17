package com.uth.mobileBE.services;

import com.uth.mobileBE.Utils.LoanSpecification;
import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.CreateLoanWithDetailsRequest;
import com.uth.mobileBE.dto.request.LoanRequest;
import com.uth.mobileBE.dto.response.LoanDetailResponse;
import com.uth.mobileBE.dto.response.LoanResponse;
import com.uth.mobileBE.dto.response.ViolationResponse;
import com.uth.mobileBE.exceptions.ReaderHasActiveViolationsException;
import com.uth.mobileBE.mapper.ViolationMapper;
import com.uth.mobileBE.models.BookCopy;
import com.uth.mobileBE.models.Loan;
import com.uth.mobileBE.models.LoanDetail;
import com.uth.mobileBE.models.Violation;
import com.uth.mobileBE.models.enums.StatusBookCopy;
import com.uth.mobileBE.models.enums.StatusLoan;
import com.uth.mobileBE.models.enums.StatusLoanDetail;
import com.uth.mobileBE.models.enums.StatusViolation;
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
    @Autowired private LoanDetailService loanDetailService;

    @Autowired private BookCopyRepository bookCopyRepository;
    @Autowired private ViolationRepository violationRepository;
    @Autowired private LoanPolicyRepository loanPolicyRepository;
    @Autowired private LoanDetailRepository loanDetailRepository;
    @Autowired
    private ViolationMapper violationMapper;


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


    @Transactional
    public LoanResponse createLoanWithDetails(CreateLoanWithDetailsRequest request) {
        Long libaryId= SecurityUtils.getLibraryId();
        String username = SecurityUtils.getUsername();
        var library = libraryRepository.findById(libaryId)
                .orElseThrow(() -> new RuntimeException("Thư viện không tồn tại"));
        var reader = readerRepository.findById(request.getReaderId())
                .orElseThrow(() -> new RuntimeException("Người đọc không tồn tại"));
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));


        // Kiểm tra các Violation đang ACTIVE
        List<Violation> activeViolations = violationRepository.findByReader_ReaderIdAndStatus(
                reader.getReaderId(), StatusViolation.ACTIVE);

        if (!activeViolations.isEmpty()) {
            // Ném lỗi kèm theo danh sách vi phạm để Controller bắt và trả về cho Client
            throw new ReaderHasActiveViolationsException(
                    "Độc giả đang có vi phạm chưa xử lý. Không thể mượn sách.", violationMapper.toResponseList(activeViolations));
        }

        if (reader.getIsBlocked() != null && reader.getIsBlocked()) {
            throw new RuntimeException("Độc giả này đang bị khóa tài khoản.");
        }

        Loan loan = Loan.builder()
                .library(library)
                .reader(reader)
                .processedBy(user)
                .borrowDate(LocalDateTime.now())
                .status(StatusLoan.ACTIVE)
                .build();

        loan = loanRepository.save(loan);

        if (loan.getLoanDetails() == null) {
            loan.setLoanDetails(new java.util.ArrayList<>());
        }

        // Khởi tạo các Chi tiết Phiếu Mượn (LoanDetail)
        for (Long copyId : request.getCopyIds()) {
            BookCopy copy = bookCopyRepository.findById(copyId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mã sách: " + copyId));

            if (copy.getStatus() != StatusBookCopy.AVAILABLE) {
                throw new RuntimeException("Sách (Copy ID: " + copyId + ") hiện không khả dụng để mượn.");
            }

            // Tính toán số ngày mượn tối đa dựa trên Policy
            int maxBorrowDays = calculateMaxBorrowDays(library.getLibraryId(), copy);

            // Tạo LoanDetail
            LoanDetail detail = LoanDetail.builder()
                    .loan(loan)
                    .bookCopy(copy)
                    .dueDate(LocalDateTime.now().plusDays(maxBorrowDays))
                    .status(StatusLoanDetail.BORROWING)
                    .build();

            // Cập nhật trạng thái sách
            copy.setStatus(StatusBookCopy.BORROWED);

            bookCopyRepository.save(copy);
            loanDetailRepository.save(detail);
            loan.getLoanDetails().add(detail);
        }

        return mapToResponse(loan);
    }

    // Hàm phụ trợ tính số ngày mượn
    private int calculateMaxBorrowDays(Long libraryId, BookCopy copy) {
        Long categoryId = copy.getBook().getCategory() != null ? copy.getBook().getCategory().getCategoryId() : null;

        if (categoryId != null) {
            // Ưu tiên tìm policy riêng cho category này
            var categoryPolicy = loanPolicyRepository.findByLibrary_LibraryIdAndCategory_CategoryId(libraryId, categoryId);
            if (categoryPolicy.isPresent() && categoryPolicy.get().getMaxBorrowDays() != null) {
                return categoryPolicy.get().getMaxBorrowDays();
            }
        }

        // Fallback tìm policy chung (category = null) của thư viện
        var defaultPolicy = loanPolicyRepository.findByLibrary_LibraryIdAndCategoryIsNull(libraryId);
        if (defaultPolicy.isPresent() && defaultPolicy.get().getMaxBorrowDays() != null) {
            return defaultPolicy.get().getMaxBorrowDays();
        }

        //trả về mặc định nếu thư viện chưa cấu hình bất kỳ policy nào
        return 30;
    }

    @Transactional // Đã bỏ readOnly = true để cho phép lưu trạng thái OVERDUE mới phát hiện
    public LoanResponse getLoanById(Long id) {
        // 1. Tìm phiếu mượn gốc
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn gốc với ID: " + id));

        // 2. Ép các dòng sách (LoanDetail) kiểm tra quá hạn
        if (loan.getLoanDetails() != null && !loan.getLoanDetails().isEmpty()) {
            for (LoanDetail detail : loan.getLoanDetails()) {
                // Hàm này bên LoanDetailService đã có lệnh loanDetailRepository.save(detail)
                loanDetailService.evaluateAndApplyOverdue(detail);
            }
        }

        // 3. Sau khi các con đã cập nhật xong, đồng bộ trạng thái cho thằng cha (Loan)
        // Hàm này sẽ quét lại các con trong DB và save(loan)
        loanDetailService.syncLoanStatus(id);

        // 4. Trả về kết quả đã được làm sạch và cập nhật
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