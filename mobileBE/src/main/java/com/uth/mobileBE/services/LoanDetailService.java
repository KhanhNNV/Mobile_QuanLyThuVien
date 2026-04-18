package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.FeeInvoiceRequest;
import com.uth.mobileBE.dto.request.LoanDetailRequest;
import com.uth.mobileBE.dto.request.UpdateLoanDetailRequest;
import com.uth.mobileBE.dto.request.ViolationRequest;
import com.uth.mobileBE.dto.response.LoanDetailResponse;
import com.uth.mobileBE.events.LoanStatusSyncEvent;
import com.uth.mobileBE.models.BookCopy;
import com.uth.mobileBE.models.FeeConfig;
import com.uth.mobileBE.models.Loan;
import com.uth.mobileBE.models.LoanDetail;
import com.uth.mobileBE.models.enums.*;
import com.uth.mobileBE.repositories.BookCopyRepository;
import com.uth.mobileBE.repositories.FeeConfigRepository;
import com.uth.mobileBE.repositories.LoanDetailRepository;
import com.uth.mobileBE.repositories.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.context.event.EventListener;
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
    private final FeeConfigRepository feeConfigRepository;
    private final FeeInvoiceService feeInvoiceService;

    @EventListener
    public void handleLoanStatusSyncEvent(LoanStatusSyncEvent event) {
        // Lấy loanId từ Event và gọi hàm đồng bộ
        this.syncLoanStatus(event.getLoanId());
    }

    @Transactional
    public List<LoanDetailResponse> getAllDetails() {
        List<LoanDetail> details = loanDetailRepository.findAll();
        details.forEach(this::evaluateAndApplyOverdue);
        return details.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanDetailResponse updateDetailAdmin(Long loanDetailId, UpdateLoanDetailRequest request) {
        LoanDetail detail = loanDetailRepository.findById(loanDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết phiếu mượn"));

        Long oldCopyId = detail.getBookCopy().getCopyId();
        Long newCopyId = request.getCopyId();

        if (!oldCopyId.equals(newCopyId)) {
            BookCopy newCopy = bookCopyRepository.findById(newCopyId)
                    .orElseThrow(() -> new RuntimeException("Sách thay thế không tồn tại"));

            BookCopy oldCopy = detail.getBookCopy();
            oldCopy.setStatus(StatusBookCopy.AVAILABLE);
            bookCopyRepository.save(oldCopy);

            detail.setBookCopy(newCopy);
            if ("BORROWING".equals(request.getStatus())) {
                newCopy.setStatus(StatusBookCopy.BORROWED);
            }
            bookCopyRepository.save(newCopy);
        }

        StatusLoanDetail newStatus = StatusLoanDetail.valueOf(request.getStatus());
        detail.setStatus(newStatus);
        BookCopy currentCopy = detail.getBookCopy();

        if (newStatus == StatusLoanDetail.DAMAGED && request.getCondition() != null) {
            currentCopy.setCondition(ConditionBookCopy.valueOf(request.getCondition()));
        } else if (newStatus != StatusLoanDetail.DAMAGED) {
            if (currentCopy.getCondition() == ConditionBookCopy.FAIR ||
                    currentCopy.getCondition() == ConditionBookCopy.POOR) {
                currentCopy.setCondition(ConditionBookCopy.GOOD);
            }
        }

        // Cập nhật trạng thái (Chờ thanh toán hóa đơn mới được gỡ)
        if (newStatus == StatusLoanDetail.LOST) {
            currentCopy.setStatus(StatusBookCopy.LOST);
            detail.setReturnDate(null);
            createViolationForDetail(detail, "Khách làm mất sách");
        } else if (newStatus == StatusLoanDetail.DAMAGED) {
            detail.setReturnDate(LocalDateTime.now());
            currentCopy.setStatus(StatusBookCopy.DAMAGED);
            createViolationForDetail(detail, "Sách bị hư hỏng");
        } else if (newStatus == StatusLoanDetail.RETURNED) {
            detail.setReturnDate(LocalDateTime.now());
            currentCopy.setStatus(StatusBookCopy.AVAILABLE);
        } else if (newStatus == StatusLoanDetail.BORROWING) {
            detail.setReturnDate(null);
            currentCopy.setStatus(StatusBookCopy.BORROWED);
        }

        bookCopyRepository.save(currentCopy);

        if (request.getDueDate() != null) {
            detail.setDueDate(request.getDueDate());
        }

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

        LocalDateTime calculatedDueDate = LocalDateTime.now().plusDays(request.getBorrowDays());

        LoanDetail detail = LoanDetail.builder()
                .loan(loan)
                .bookCopy(copy)
                .dueDate(calculatedDueDate)
                .status(StatusLoanDetail.BORROWING)
                .build();

        copy.setStatus(StatusBookCopy.BORROWED);
        bookCopyRepository.save(copy);

        return mapToResponse(loanDetailRepository.save(detail));
    }

    /**
     * Logic khi khách mang sách tới quầy trả.
     * Nếu có lỗi (Trễ, Hư), hệ thống sinh Violation và neo ở trạng thái lỗi.
     */
    @Transactional
    public LoanDetailResponse returnBook(Long loanDetailId, ConditionBookCopy returnCondition) {
        LoanDetail detail = loanDetailRepository.findById(loanDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết phiếu mượn"));

        if (detail.getStatus() == StatusLoanDetail.LOST) {
            throw new RuntimeException("Sách này đã bị khóa ở trạng thái MẤT. Vui lòng thanh toán hóa đơn MẤT SÁCH để hoàn tất.");
        }

        LocalDateTime now = LocalDateTime.now();
        detail.setReturnDate(now);
        BookCopy copy = detail.getBookCopy();

        if (returnCondition != null) {
            copy.setCondition(returnCondition);
        }

        boolean isDamaged = (returnCondition == ConditionBookCopy.FAIR || returnCondition == ConditionBookCopy.POOR);
        boolean isOverdue = detail.getDueDate() != null && now.isAfter(detail.getDueDate());

        // --- LOGIC TÍNH TIỀN PHẠT & TẠO HÓA ĐƠN ---
        double totalPenaltyAmount = 0.0;
        StringBuilder violationReason = new StringBuilder();

        // Lấy giá gốc của sách
        double basePrice = copy.getBook().getBasePrice();

        // 1. Phạt trễ hạn
        if (isOverdue) {
            long daysOverdue = ChronoUnit.DAYS.between(detail.getDueDate().toLocalDate(), now.toLocalDate());
            if (daysOverdue <= 0) {
                daysOverdue = 1;
            }
            // Tính tiền: số ngày * phí trễ 1 ngày
            FeeConfig feeConfig = feeConfigRepository.findByLibrary_LibraryIdAndFeeType(detail.getLoan().getLibrary().getLibraryId(),TypeFeeConfig.LATE_PER_DAY)
                    .orElseThrow(()-> new RuntimeException("Chưa có phí phạt trễ hạn / ngày cho thư viện này"));
            double overdueFee = daysOverdue * feeConfig.getAmount();


            totalPenaltyAmount += overdueFee;
            violationReason.append("Trả sách trễ hạn ").append(daysOverdue)
                    .append(" ngày (Phí: ").append(overdueFee).append("). ");
        }

        // 2. Phạt hư hỏng sách vật lý
        if (isDamaged) {
            // Tính tiền: giá sách + phụ phí hư hỏng
            FeeConfig feeConfig = feeConfigRepository.findByLibrary_LibraryIdAndFeeType(detail.getLoan().getLibrary().getLibraryId(),TypeFeeConfig.DAMAGE_FEE)
                    .orElseThrow(()-> new RuntimeException("Chưa có phí phạt hư hại sách cho thư viện này"));
            double damageFee = basePrice + feeConfig.getAmount();

            totalPenaltyAmount += damageFee;
            violationReason.append("Sách bị hư hỏng (Tình trạng: ").append(returnCondition)
                    .append(", Phí: ").append(damageFee).append("). ");
            copy.setStatus(StatusBookCopy.DAMAGED);
        } else {
            copy.setStatus(StatusBookCopy.AVAILABLE);
        }

        // --- CHỐT TRẠNG THÁI VÀ GỌI SERVICE ---
        if (totalPenaltyAmount > 0) {
            // Đứng im ở trạng thái lỗi, chưa được RETURNED
            detail.setStatus(isDamaged ? StatusLoanDetail.DAMAGED : StatusLoanDetail.OVERDUE);

            String reasonStr = violationReason.toString().trim();


            // 2. Tạo Hóa đơn phạt (Để yêu cầu thanh toán)
            createPenaltyInvoice(detail, totalPenaltyAmount, reasonStr);

        } else {
            // Không lỗi lầm gì -> Trả thành công
            detail.setStatus(StatusLoanDetail.RETURNED);
        }

        bookCopyRepository.save(copy);
        loanDetailRepository.save(detail);
        syncLoanStatus(detail.getLoan().getLoanId());

        return mapToResponse(detail);
    }


    /**
     * HÀM MỚI: Được gọi khi Hóa Đơn (Invoice) đã được thanh toán thành công.
     * Hoàn tất quá trình: Đổi Phiếu -> Đã trả, Đổi Violation -> RESOLVED
     */
    @Transactional
    public LoanDetailResponse resolvePaymentAndCompleteDetail(Long loanDetailId) {
        LoanDetail detail = loanDetailRepository.findById(loanDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết phiếu mượn"));

        // 1. Chuyển trạng thái phiếu chi tiết về ĐÃ TRẢ (Hoàn thành)
        detail.setStatus(StatusLoanDetail.RETURNED);
        loanDetailRepository.save(detail);

        // 2. Chuyển Violation sang RESOLVED
        // (BẠN CẦN THÊM HÀM resolveViolationByLoanId TRONG ViolationService)
        violationService.resolveViolationByLoanId(detail.getLoan().getLoanId());

        // 3. Cập nhật lại trạng thái Phiếu mượn tổng
        syncLoanStatus(detail.getLoan().getLoanId());

        return mapToResponse(detail);
    }

    public void evaluateAndApplyOverdue(LoanDetail detail) {
        if (detail.getStatus() == StatusLoanDetail.BORROWING && detail.getDueDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(detail.getDueDate())) {
                long daysOverdue = ChronoUnit.DAYS.between(detail.getDueDate(), now);

                if (daysOverdue > 15) {
                    detail.setStatus(StatusLoanDetail.LOST);
                    BookCopy copy = detail.getBookCopy();
                    copy.setStatus(StatusBookCopy.LOST);
                    bookCopyRepository.save(copy);

                    double basePrice = detail.getBookCopy().getBook().getBasePrice();
                    FeeConfig feeConfig = feeConfigRepository.findByLibrary_LibraryIdAndFeeType(detail.getLoan().getLibrary().getLibraryId(),TypeFeeConfig.LOST_BOOK)
                            .orElseThrow(()-> new RuntimeException("Chưa có phí phạt mất sách cho thư viện này"));
                    double lostFee = basePrice * feeConfig.getAmount();
                    String reason = "Sách trễ hạn quá 15 ngày, tự động đánh dấu LOST. Phí: " + lostFee;

                    createViolationForDetail(detail, reason);
                    createPenaltyInvoice(detail, lostFee, reason); // <--- Gọi tạo hóa đơn ở đây

                    loanDetailRepository.save(detail);
                } else {
                    if (detail.getStatus() == StatusLoanDetail.BORROWING) {
                        detail.setStatus(StatusLoanDetail.OVERDUE);

                        // Tạo Violation trễ hạn DUY NHẤT 1 LẦN tại đây
                        createViolationForDetail(detail, "Có sách trễ hạn, yêu cầu độc giả trả sách và thanh toán phí phạt");

                        loanDetailRepository.save(detail);
                        syncLoanStatus(detail.getLoan().getLoanId());
                    }
                    // Nếu trạng thái đã là OVERDUE rồi thì bỏ qua, không tạo thêm Violation nữa.
                }

                loanDetailRepository.save(detail);
                syncLoanStatus(detail.getLoan().getLoanId());
            }
        }
    }

    private void createViolationForDetail(LoanDetail detail, String reason) {
        ViolationRequest vReq = ViolationRequest.builder()
                .readerId(detail.getLoan().getReader().getReaderId())
                .libraryId(detail.getLoan().getLibrary().getLibraryId())
                .loanId(detail.getLoan().getLoanId())
                .loanDetailId(detail.getLoanDetailId())
                .reason(reason)
                .status("ACTIVE") // Mới tạo luôn là ACTIVE
                .build();

        violationService.createViolation(vReq);
    }

    public void syncLoanStatus(Long loanId) {
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
                loan.setStatus(StatusLoan.COMPLETED); // Tất cả đã RETURNED thì sẽ nhảy vào đây
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

    private void createPenaltyInvoice(LoanDetail detail, double amount, String description) {
        FeeInvoiceRequest invoiceReq = FeeInvoiceRequest.builder()
                .loanDetailId(detail.getLoanDetailId())
                .readerId(detail.getLoan().getReader().getReaderId())
                .totalAmount(amount)
                .type(TypeFeeInvoice.PENALTY) // TypeFeeInvoice là PENALTY theo yêu cầu
                .description(description)
                .status(StatusFeeInvoice.UNPAID) // Đang chờ thanh toán
                .build();

        feeInvoiceService.createFeeInvoice(invoiceReq);
    }

    public Long getLoanIdByLoanDetailId(Long loanDetailId) {
        LoanDetail loanDetail = loanDetailRepository.findById(loanDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy LoanDetail"));

        if (loanDetail.getLoan() == null) {
            throw new RuntimeException("LoanDetail chưa gắn với Loan");
        }

        return loanDetail.getLoan().getLoanId();
    }

    /**
     * Lấy danh sách phân trang sách đã mượn của độc giả theo trạng thái.
     *
     * @param readerId ID độc giả
     * @param status   Trạng thái chi tiết mượn (BORROWING, RETURNED, ...)
     * @param page     Số trang (bắt đầu từ 0)
     * @param size     Số lượng mỗi trang
     */
    public Page<LoanDetailResponse> getReaderLoanDetails(Long readerId, StatusLoanDetail status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").descending());
        Page<LoanDetail> loanDetails = loanDetailRepository.findByLoan_Reader_ReaderIdAndStatus(readerId, status, pageable);

        return loanDetails.map(this::mapToResponse);
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