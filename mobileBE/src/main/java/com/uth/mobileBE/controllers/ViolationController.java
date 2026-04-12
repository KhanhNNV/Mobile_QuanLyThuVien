package com.uth.mobileBE.controllers;

import com.uth.mobileBE.dto.request.ViolationRequest;
import com.uth.mobileBE.dto.response.ViolationResponse;
import com.uth.mobileBE.services.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/violations")
@RequiredArgsConstructor
public class ViolationController {
    private final ViolationService violationService;

    @PostMapping
    public ResponseEntity<ViolationResponse> create(@RequestBody ViolationRequest request) {
        return ResponseEntity.ok(violationService.createViolation(request));
    }
}