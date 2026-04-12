package com.uth.mobileBE.controllers;

import com.uth.mobileBE.dto.request.LoanRequest;
import com.uth.mobileBE.dto.response.LoanResponse;
import com.uth.mobileBE.services.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired private LoanService loanService;

    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAll() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @PostMapping
    public ResponseEntity<LoanResponse> create(@RequestBody LoanRequest request) {
        return new ResponseEntity<>(loanService.createLoan(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanResponse> update(@PathVariable Long id, @RequestBody LoanRequest request) {
        return ResponseEntity.ok(loanService.updateLoan(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/library/{libraryId}/borrowing/count")
    public ResponseEntity<Long> countBorrowingLoans(@PathVariable Long libraryId) {
        return ResponseEntity.ok(loanService.countBorrowingLoans(libraryId));
    }

    @GetMapping("/library/{libraryId}/overdue/count")
    public ResponseEntity<Long> countOverdueLoans(@PathVariable Long libraryId) {
        return ResponseEntity.ok(loanService.countOverdueLoans(libraryId));
    }
}