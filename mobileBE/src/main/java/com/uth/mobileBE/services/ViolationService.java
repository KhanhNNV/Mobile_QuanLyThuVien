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

    public List<String> getViolationQuantityAlerts(Long libraryId) {
        Long count = violationRepository.countByLibrary_LibraryIdAndStatus(libraryId,StatusViolation.ACTIVE);

        return List.of("Hiện có " + count + " vi phạm chưa được giải quyết");
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