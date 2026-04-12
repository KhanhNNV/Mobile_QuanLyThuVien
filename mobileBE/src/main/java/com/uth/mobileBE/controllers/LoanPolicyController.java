package com.uth.mobileBE.controllers;

import com.uth.mobileBE.dto.request.LoanPolicyRequest;
import com.uth.mobileBE.dto.response.LoanPolicyResponse;
import com.uth.mobileBE.services.LoanPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-policies")
public class LoanPolicyController {

    @Autowired
    private LoanPolicyService loanPolicyService;

    @PostMapping
    public ResponseEntity<LoanPolicyResponse> createLoanPolicy(@RequestBody LoanPolicyRequest request) {
        LoanPolicyResponse response = loanPolicyService.createLoanPolicy(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LoanPolicyResponse>> getAllLoanPolicies() {
        return ResponseEntity.ok(loanPolicyService.getAllLoanPolicies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanPolicyResponse> getLoanPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(loanPolicyService.getLoanPolicyById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanPolicyResponse> updateLoanPolicy(
            @PathVariable Long id,
            @RequestBody LoanPolicyRequest request) {
        return ResponseEntity.ok(loanPolicyService.updateLoanPolicy(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoanPolicy(@PathVariable Long id) {
        loanPolicyService.deleteLoanPolicy(id);
        return ResponseEntity.noContent().build();
    }
}