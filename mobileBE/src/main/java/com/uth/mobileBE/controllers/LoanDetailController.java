package com.uth.mobileBE.controllers;

import com.uth.mobileBE.dto.request.LoanDetailRequest;
import com.uth.mobileBE.dto.response.LoanDetailResponse;
import com.uth.mobileBE.services.LoanDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-details")
public class LoanDetailController {

    @Autowired private LoanDetailService loanDetailService;

    @GetMapping
    public ResponseEntity<List<LoanDetailResponse>> getAll() {
        return ResponseEntity.ok(loanDetailService.getAllDetails());
    }

    @PostMapping
    public ResponseEntity<LoanDetailResponse> create(@RequestBody LoanDetailRequest request) {
        return ResponseEntity.ok(loanDetailService.createDetail(request));
    }

    @PutMapping("/loan/{loanId}/copy/{copyId}")
    public ResponseEntity<LoanDetailResponse> update(
            @PathVariable Long loanId,
            @PathVariable Long copyId,
            @RequestBody LoanDetailRequest request) {
        return ResponseEntity.ok(loanDetailService.updateDetail(loanId, copyId, request));
    }

    @DeleteMapping("/loan/{loanId}/copy/{copyId}")
    public ResponseEntity<Void> delete(@PathVariable Long loanId, @PathVariable Long copyId) {
        loanDetailService.deleteDetail(loanId, copyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/library/{libraryId}/alerts/due-today")
    public ResponseEntity<List<String>> getDueTodayAlerts(@PathVariable Long libraryId) {
        return ResponseEntity.ok(loanDetailService.getDueTodayAlerts(libraryId));
    }
}