package com.uth.mobileBE.services;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.ViolationRequest;
import com.uth.mobileBE.dto.response.ViolationResponse;
import com.uth.mobileBE.models.*;
import com.uth.mobileBE.models.enums.StatusViolation;
import com.uth.mobileBE.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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

    @Transactional(readOnly = true)
    public Page<ViolationResponse> getViolations(String search, StatusViolation status, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Long libraryId= SecurityUtils.getLibraryId();

        // Sắp xếp mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Thử parse keyword sang ID để tìm theo violationId
        Long searchId = null;
        if (search != null && !search.trim().isEmpty()) {
            try {
                searchId = Long.parseLong(search.trim());
            } catch (NumberFormatException ignored) {
                // search không phải là ID dạng số, giữ searchId = null
            }
        }
        Page<Violation> violationPage=violationRepository.findViolationsWithFilters(libraryId,status, startDate, endDate, search, searchId, pageable);

        return violationPage.map(this::mapToViolationResponse);
    }

    @Transactional
    public ViolationResponse updateViolation(Long id, String reason, StatusViolation status) {
        Violation violation = violationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vi phạm với ID: " + id));

        if (reason != null) {
            violation.setReason(reason);
        }
        if (status != null) {
            violation.setStatus(status);
        }
        violation.setUpdatedAt(LocalDateTime.now());

        return mapToViolationResponse(violationRepository.save(violation));
    }

    @Transactional
    public void deleteViolation(Long id) {
        if (!violationRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy vi phạm với ID: " + id);
        }
        violationRepository.deleteById(id);
    }

    private ViolationResponse mapToViolationResponse(Violation v) {
        return ViolationResponse.builder()
                                .violationId(v.getViolationId())
                                .readerId(v.getReader().getReaderId())
                                .readerName(v.getReader().getFullName())
                                .barcode(v.getReader().getBarcode())
                                .reason(v.getReason())
                                .status(v.getStatus().name())
                                .loanDetailId(v.getLoanDetail().getLoanDetailId())
                                .loanId(v.getLoan().getLoanId())
                                .createdAt(v.getCreatedAt())
                                .updatedAt(v.getUpdatedAt())
                                .build();
    }
}