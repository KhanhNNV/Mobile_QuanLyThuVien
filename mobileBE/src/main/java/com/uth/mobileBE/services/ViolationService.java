package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.ViolationRequest;
import com.uth.mobileBE.dto.response.ViolationResponse;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.Reader;
import com.uth.mobileBE.models.Violation;
import com.uth.mobileBE.models.enums.StatusViolation;
import com.uth.mobileBE.repositories.LibraryRepository;
import com.uth.mobileBE.repositories.ReaderRepository;
import com.uth.mobileBE.repositories.ViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViolationService {
    private final ViolationRepository violationRepository;
    private final ReaderRepository readerRepository;
    private final LibraryRepository libraryRepository;

    @Transactional
    public ViolationResponse createViolation(ViolationRequest request) {
        Reader reader = readerRepository.findById(request.getReaderId())
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả"));

        Library library = libraryRepository.findById(request.getLibraryId())
                                           .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện"));

        Violation violation = Violation.builder()
                                       .reader(reader)
                                       .library(library)
                                       .reason(request.getReason())
                                       .status(StatusViolation.ACTIVE)
                                       .build();

        Violation saved = violationRepository.save(violation);
        return mapToViolationResponse(saved);
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