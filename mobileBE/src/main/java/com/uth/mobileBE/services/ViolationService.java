package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.ViolationRequest;
import com.uth.mobileBE.dto.response.ViolationResponse;
import com.uth.mobileBE.models.*;
import com.uth.mobileBE.models.enums.StatusViolation;
import com.uth.mobileBE.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViolationService {
    private final ViolationRepository violationRepository;
    private final ReaderRepository readerRepository;
    private final LibraryRepository libraryRepository;
    private final LoanRepository loanRepository;
    private final LoanDetailRepository loanDetailRepository;
    private final JsonMapper.Builder builder;

    @Transactional
    public ViolationResponse createViolation(ViolationRequest request) {
        Reader reader = readerRepository.findById(request.getReaderId())
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả"));

        Library library = libraryRepository.findById(request.getLibraryId())
                                           .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện"));

        Violation.ViolationBuilder builder = Violation.builder()
                .reader(reader)
                .library(library)
                .reason(request.getReason())
                .status(StatusViolation.ACTIVE);

        // Chỉ tìm và gắn Loan nếu loanId được gửi lên
        if (request.getLoanId() != null) {
            Loan loan = loanRepository.findById(request.getLoanId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Phiếu mượn với ID: " + request.getLoanId()));
            builder.loan(loan);
        }

        if (request.getLoanDetailId() != null) {
            LoanDetail loanDetail = loanDetailRepository.findById(request.getLoanDetailId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Phiếu mượn chi tiết với ID: " + request.getLoanDetailId()));
            builder.loanDetail(loanDetail);
        }

        Violation saved = violationRepository.save(builder.build());
        return mapToViolationResponse(saved);
    }

    public void resolveViolationByLoanId(Long loanid) {
        Violation violation = violationRepository.findByLoan_LoanId(loanid).orElseThrow(()->
                new RuntimeException("Không tìm thấy phiếu mượn của vi phạm này"));
        violation.setStatus(StatusViolation.RESOLVED);
        violation.setUpdatedAt(LocalDateTime.now());
        violationRepository.save(violation);
    }



    // --- BỔ SUNG CRUD ---

    public List<ViolationResponse> getAllViolations() {
        return violationRepository.findAll().stream()
                                  .map(this::mapToViolationResponse)
                                  .collect(Collectors.toList());
    }

    public ViolationResponse getViolationById(Long id) {
        Violation violation = violationRepository.findById(id)
                                                 .orElseThrow(() -> new RuntimeException("Không tìm thấy biên bản vi phạm"));
        return mapToViolationResponse(violation);
    }

    public List<ViolationResponse> getViolationsByReaderId(Long readerId) {
        return violationRepository.findByReaderReaderId(readerId).stream()
                                  .map(this::mapToViolationResponse)
                                  .collect(Collectors.toList());
    }

    @Transactional
    public ViolationResponse updateViolation(Long id, ViolationRequest request) {
        Violation violation = violationRepository.findById(id)
                                                 .orElseThrow(() -> new RuntimeException("Không tìm thấy biên bản vi phạm để cập nhật"));

        if (request.getReason() != null) {
            violation.setReason(request.getReason());
        }

        if (request.getStatus() != null) {
            try {
                violation.setStatus(StatusViolation.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trạng thái vi phạm không hợp lệ (Chỉ nhận ACTIVE hoặc RESOLVED)");
            }
        }

        Violation updated = violationRepository.save(violation);
        return mapToViolationResponse(updated);
    }

    @Transactional
    public void deleteViolation(Long id) {
        if (!violationRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy biên bản vi phạm để xóa");
        }
        violationRepository.deleteById(id);
    }

    private ViolationResponse mapToViolationResponse(Violation v) {
        return ViolationResponse.builder()
                                .violationId(v.getViolationId())
                                .readerId(v.getReader().getReaderId())
                                .readerName(v.getReader().getFullName())
                                .reason(v.getReason())
                                .status(v.getStatus().name())
                                .createdAt(v.getCreatedAt())
                                .updatedAt(v.getUpdatedAt())
                                .build();
    }
}