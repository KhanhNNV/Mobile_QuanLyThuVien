package com.uth.mobileBE.controllers;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.CreateLoanWithDetailsRequest;
import com.uth.mobileBE.dto.request.LoanRequest;
import com.uth.mobileBE.dto.response.LoanResponse;
import com.uth.mobileBE.services.LoanService;
import jakarta.validation.Valid; // Thêm import này cho Validation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired private LoanService loanService;

    @GetMapping("/filter")
    public ResponseEntity<List<LoanResponse>> getFilteredLoans(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String search) {

        Long currentLibraryId = SecurityUtils.getLibraryId();

        List<LoanResponse> results = loanService.getLoansWithFilter(
                currentLibraryId, status, fromDate, toDate, search
        );

        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @PostMapping
    public ResponseEntity<LoanResponse> create(@Valid @RequestBody LoanRequest request) { // Thêm @Valid
        return new ResponseEntity<>(loanService.createLoan(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanResponse> update(@PathVariable Long id, @Valid @RequestBody LoanRequest request) { // Thêm @Valid
        return ResponseEntity.ok(loanService.updateLoan(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active/count")
    public ResponseEntity<Long> countBorrowingLoans() {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(loanService.countActiveLoans(libraryId)); // Gọi đúng tên hàm mới ở Service
    }

    @GetMapping("/overdue/count")
    public ResponseEntity<Long> countOverdueLoans() {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(loanService.countOverdueLoans(libraryId));
    }

    @PostMapping("/create-with-details")
    public ResponseEntity<?> createLoanWithDetails(@RequestBody CreateLoanWithDetailsRequest request) {
        LoanResponse response = loanService.createLoanWithDetails(request);
        return ResponseEntity.ok(response);
    }
}