package com.uth.mobileBE.controllers;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.LoanPolicyRequest;
import com.uth.mobileBE.dto.response.LoanPolicyResponse;
import com.uth.mobileBE.services.LoanPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-policies")
public class LoanPolicyController {

    @Autowired
    private LoanPolicyService loanPolicyService;

    @GetMapping
    public ResponseEntity<List<LoanPolicyResponse>> getAllPolicies() {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(loanPolicyService.getPoliciesByLibrary(libraryId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LoanPolicyResponse> createPolicy(@RequestBody LoanPolicyRequest request) {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(loanPolicyService.createPolicy(libraryId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<LoanPolicyResponse> updatePolicy(
            @PathVariable Long id,
            @RequestBody LoanPolicyRequest request) {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(loanPolicyService.updatePolicy(id, libraryId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        Long libraryId = SecurityUtils.getLibraryId();
        loanPolicyService.deletePolicy(id, libraryId);
        return ResponseEntity.noContent().build();
    }

}