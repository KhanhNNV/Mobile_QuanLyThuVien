package com.uth.mobileBE.controllers;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.LoanDetailRequest;
import com.uth.mobileBE.dto.request.UpdateLoanDetailRequest;
import com.uth.mobileBE.dto.response.LoanDetailResponse;
import com.uth.mobileBE.models.enums.ConditionBookCopy;
import com.uth.mobileBE.services.LoanDetailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-details")
public class LoanDetailController {

    @Autowired
    private LoanDetailService loanDetailService;

    @GetMapping
    public ResponseEntity<List<LoanDetailResponse>> getAll() {
        return ResponseEntity.ok(loanDetailService.getAllDetails());
    }

    // Thêm API này vào LoanDetailController
    @PutMapping("/{loanDetailId}/admin-update")
    public ResponseEntity<LoanDetailResponse> updateDetailAdmin(
            @PathVariable Long loanDetailId,
            @Valid @RequestBody UpdateLoanDetailRequest request) {
        return ResponseEntity.ok(loanDetailService.updateDetailAdmin(loanDetailId, request));
    }

    // Đã thêm @Valid để kiểm tra dữ liệu đầu vào (borrowDays, loanId, copyId)
    @PostMapping
    public ResponseEntity<LoanDetailResponse> create(@Valid @RequestBody LoanDetailRequest request) {
        return ResponseEntity.ok(loanDetailService.createDetail(request));
    }


    @PutMapping("/{loanDetailId}/return")
    public ResponseEntity<LoanDetailResponse> returnBook(
            @PathVariable Long loanDetailId,
            @RequestParam(required = false) ConditionBookCopy condition) {

        return ResponseEntity.ok(loanDetailService.returnBook(loanDetailId, condition));
    }


    @DeleteMapping("/{loanDetailId}")
    public ResponseEntity<Void> delete(@PathVariable Long loanDetailId) {
        loanDetailService.deleteDetail(loanDetailId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts/due-today")
    public ResponseEntity<List<String>> getDueTodayAlerts() {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(loanDetailService.getDueTodayAlerts(libraryId));
    }
}