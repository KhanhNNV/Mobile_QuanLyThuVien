package com.uth.mobileBE.controllers;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.FeeInvoiceRequest;
import com.uth.mobileBE.dto.response.FeeInvoiceResponse;
import com.uth.mobileBE.models.enums.StatusFeeInvoice;
import com.uth.mobileBE.services.FeeInvoiceService;
import com.uth.mobileBE.services.LoanDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fee-invoices")
public class FeeInvoiceController {

    @Autowired
    private FeeInvoiceService feeInvoiceService;

    //API tìm kiếm hóa đơn chi tiết cho phiếu vi phạm
    @GetMapping("/loan-detail/{loanDetailId}")
    public ResponseEntity<FeeInvoiceResponse> getInvoiceByLoanDetailId(
            @PathVariable("loanDetailId") Long loanDetailId
    ) {
        return ResponseEntity.ok(feeInvoiceService.getInvoiceByLoanDetailId(loanDetailId));
    }


    @GetMapping()
    public ResponseEntity<List<FeeInvoiceResponse>> getInvoicesByLibrary() {
        Long libraryId = SecurityUtils.getLibraryId();
        List<FeeInvoiceResponse> invoices = feeInvoiceService.getInvoicesByLibrary(libraryId);

        if (invoices.isEmpty()) {
            return ResponseEntity.noContent().build(); // Trả về 204 nếu không có dữ liệu
        }

        return ResponseEntity.ok(invoices); // Trả về 200 kèm danh sách hóa đơn
    }

    @GetMapping("/search")
    public ResponseEntity<Page<FeeInvoiceResponse>> searchInvoices(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) StatusFeeInvoice status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir
    ) {
        Page<FeeInvoiceResponse> responsePage = feeInvoiceService.searchAndPaginateInvoices(keyword, page, size, sortBy, sortDir,status);
        return ResponseEntity.ok(responsePage);
    }

    @PostMapping
    public ResponseEntity<FeeInvoiceResponse> createFeeInvoice(@RequestBody FeeInvoiceRequest request) {
        FeeInvoiceResponse response = feeInvoiceService.createFeeInvoice(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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