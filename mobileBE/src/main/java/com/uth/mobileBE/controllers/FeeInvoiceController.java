package com.uth.mobileBE.controllers;

import com.uth.mobileBE.dto.request.FeeInvoiceRequest;
import com.uth.mobileBE.dto.response.FeeInvoiceResponse;
import com.uth.mobileBE.services.FeeInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fee-invoices")
public class FeeInvoiceController {

    @Autowired
    private FeeInvoiceService feeInvoiceService;

    @PostMapping
    public ResponseEntity<FeeInvoiceResponse> createFeeInvoice(@RequestBody FeeInvoiceRequest request) {
        FeeInvoiceResponse response = feeInvoiceService.createFeeInvoice(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FeeInvoiceResponse>> getAllFeeInvoices() {
        return ResponseEntity.ok(feeInvoiceService.getAllFeeInvoices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeeInvoiceResponse> getFeeInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(feeInvoiceService.getFeeInvoiceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeeInvoiceResponse> updateFeeInvoice(
            @PathVariable Long id,
            @RequestBody FeeInvoiceRequest request) {
        return ResponseEntity.ok(feeInvoiceService.updateFeeInvoice(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeeInvoice(@PathVariable Long id) {
        feeInvoiceService.deleteFeeInvoice(id);
        return ResponseEntity.noContent().build();
    }
}