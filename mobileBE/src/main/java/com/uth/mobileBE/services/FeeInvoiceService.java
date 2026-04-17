package com.uth.mobileBE.services;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.FeeInvoiceRequest;
import com.uth.mobileBE.dto.response.FeeInvoiceResponse;
import com.uth.mobileBE.events.LoanStatusSyncEvent;
import com.uth.mobileBE.models.*;
import com.uth.mobileBE.models.enums.StatusFeeInvoice;
import com.uth.mobileBE.models.enums.StatusLoanDetail;
import com.uth.mobileBE.models.enums.StatusViolation;
import com.uth.mobileBE.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeeInvoiceService {

    @Autowired
    private FeeInvoiceRepository feeInvoiceRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private LoanDetailRepository loanDetailRepository;
    

    @Autowired
    private ViolationRepository violationRepository;

    private final ApplicationEventPublisher eventPublisher;

    public FeeInvoiceService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public List<FeeInvoiceResponse> getInvoicesByLibrary(Long libraryId) {
        return feeInvoiceRepository.findByLibrary_LibraryId(libraryId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FeeInvoiceResponse createFeeInvoice(FeeInvoiceRequest request) {
        // Kiểm tra và lấy các Entity liên quan dựa trên ID
        Long libraryId = SecurityUtils.getLibraryId();
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Library với id: " + request.getLibraryId()));

        Reader reader = readerRepository.findById(request.getReaderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Reader với id: " + request.getReaderId()));

        LoanDetail loanDetail = null;
        if (request.getLoanDetailId() != null) {
            loanDetail = loanDetailRepository.findById(request.getLoanDetailId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Loan với id: " + request.getLoanDetailId()));
        }

        LocalDateTime now = LocalDateTime.now();

        FeeInvoice feeInvoice = FeeInvoice.builder()
                .library(library)
                .reader(reader)
                .loanDetail(loanDetail)
                .type(request.getType())
                .totalAmount(request.getTotalAmount())
                .status(request.getStatus())
                .description(request.getDescription())
                .createdAt(now)
                .updateAt(now)
                .build();

        FeeInvoice savedInvoice = feeInvoiceRepository.save(feeInvoice);
        return mapToResponse(savedInvoice);
    }

    public List<FeeInvoiceResponse> getAllFeeInvoices() {
        return feeInvoiceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeeInvoiceResponse getFeeInvoiceById(Long id) {
        FeeInvoice feeInvoice = getFeeInvoiceEntityById(id);
        return mapToResponse(feeInvoice);
    }

    @Transactional
    public FeeInvoiceResponse updateFeeInvoice(Long id, FeeInvoiceRequest request) {
        FeeInvoice existingInvoice = getFeeInvoiceEntityById(id);

        // Cập nhật các trường thông thường
        if (request.getType() != null) {
            existingInvoice.setType(request.getType());
        }
        if (request.getTotalAmount() != null) {
            existingInvoice.setTotalAmount(request.getTotalAmount());
        }

        if (request.getLibraryId() != null) {
            Library library = libraryRepository.findById(request.getLibraryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Library"));
            existingInvoice.setLibrary(library);
        }
        if (request.getReaderId() != null) {
            Reader reader = readerRepository.findById(request.getReaderId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Reader"));
            existingInvoice.setReader(reader);
        }
        if (request.getLoanDetailId() != null) {
            LoanDetail loanDetail = loanDetailRepository.findById(request.getLoanDetailId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Loan Detail"));
            existingInvoice.setLoanDetail(loanDetail);
        }

        // XỬ LÝ LOGIC KHI ĐỔI TRẠNG THÁI SANG PAID
        if (request.getStatus() != null) {
            existingInvoice.setStatus(request.getStatus());

            if (request.getStatus().name().equals("PAID")) {
                // 1. Mở khóa cho độc giả
                Reader reader = existingInvoice.getReader();
                if (reader != null) {
                    reader.setIsBlocked(false);
                    readerRepository.save(reader);
                }

                // 2. Logic dành riêng cho hóa đơn phạt (PENALTY)
                if (existingInvoice.getType() != null && existingInvoice.getType().name().equals("PENALTY")) {
                    LoanDetail detail = existingInvoice.getLoanDetail();

                    if (detail != null) {
                        // 2.1 Cập nhật trạng thái LoanDetail
                        // Chỉ đổi thành RETURNED nếu đang là OVERDUE hoặc DAMAGED (LOST sẽ bị bỏ qua, giữ nguyên)
                        if (detail.getStatus() == StatusLoanDetail.OVERDUE || detail.getStatus() == StatusLoanDetail.DAMAGED) {
                            detail.setStatus(StatusLoanDetail.RETURNED);
                            loanDetailRepository.save(detail);

                            eventPublisher.publishEvent(new LoanStatusSyncEvent(detail.getLoan().getLoanId()));

                        }
                        // 2.2 Cập nhật trạng thái Violation (Từ ACTIVE -> RESOLVED)
                        // Gọi repository tìm các vi phạm đang ACTIVE của LoanDetail này
                        List<Violation> activeViolations = violationRepository.findByLoanDetailAndStatus(detail, StatusViolation.ACTIVE);

                        if (activeViolations != null && !activeViolations.isEmpty()) {
                            for (Violation v : activeViolations) {
                                v.setStatus(StatusViolation.RESOLVED);
                            }
                            violationRepository.saveAll(activeViolations);
                        }
                    }
                }
            }
        }

        existingInvoice.setUpdateAt(LocalDateTime.now());
        return mapToResponse(feeInvoiceRepository.save(existingInvoice));
    }

    public void deleteFeeInvoice(Long id) {
        FeeInvoice feeInvoice = getFeeInvoiceEntityById(id);
        feeInvoiceRepository.delete(feeInvoice);
    }


    // --- CÁC HÀM HỖ TRỢ DÙNG NỘI BỘ ---

    private FeeInvoice getFeeInvoiceEntityById(Long id) {
        return feeInvoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy FeeInvoice với id: " + id));
    }

    private FeeInvoiceResponse mapToResponse(FeeInvoice invoice) {
        return FeeInvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .libraryId(invoice.getLibrary().getLibraryId())
                .readerId(invoice.getReader().getReaderId())
                .readerName(invoice.getReader().getFullName())
                .loanDetailId(invoice.getLoanDetail() != null ? invoice.getLoanDetail().getLoanDetailId() : null)
                .loanId(invoice.getLoanDetail() != null && invoice.getLoanDetail().getLoan() != null ? invoice.getLoanDetail().getLoan().getLoanId() : null)
                .type(invoice.getType())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .createdAt(invoice.getCreatedAt())
                .updateAt(invoice.getUpdateAt())
                .build();
    }
}