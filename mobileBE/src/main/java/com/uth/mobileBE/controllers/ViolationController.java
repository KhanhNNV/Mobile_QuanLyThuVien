package com.uth.mobileBE.controllers;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.UpdateViolationRequest;
import com.uth.mobileBE.dto.request.ViolationRequest;
import com.uth.mobileBE.dto.response.ViolationResponse;
import com.uth.mobileBE.models.Violation;
import com.uth.mobileBE.models.enums.StatusViolation;
import com.uth.mobileBE.services.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/violations")
@RequiredArgsConstructor
public class ViolationController {
    private final ViolationService violationService;

    @PostMapping
    public ResponseEntity<ViolationResponse> create(@RequestBody ViolationRequest request) {
        return ResponseEntity.ok(violationService.createViolation(request));
    }

    // Lấy danh sách (Phân trang, tìm kiếm, lọc)
    @GetMapping
    public ResponseEntity<Page<ViolationResponse>> getViolations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StatusViolation status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ViolationResponse> violations = violationService.getViolations(search, status, startDate, endDate, page, size);
        return ResponseEntity.ok(violations);
    }

    // Cập nhật vi phạm
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ViolationResponse> updateViolation(
            @PathVariable Long id,
            @RequestBody UpdateViolationRequest request) {
        ViolationResponse updatedViolation = violationService.updateViolation(id, request.getReason(), request.getStatus());
        return ResponseEntity.ok(updatedViolation);
    }

    // Xóa vi phạm
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteViolation(@PathVariable Long id) {
        violationService.deleteViolation(id);
        return ResponseEntity.ok("Xóa vi phạm thành công");
    }


    @GetMapping("/alert")
    public ResponseEntity<List<String>> getViolationQuantityAlerts() {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(violationService.getViolationQuantityAlerts(libraryId));
    }
}