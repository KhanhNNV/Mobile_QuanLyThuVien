//package com.uth.mobileBE.controllers;
//
//import com.uth.mobileBE.Utils.SecurityUtils;
//import com.uth.mobileBE.dto.request.LoanRequest;
//import com.uth.mobileBE.dto.response.LoanResponse;
//import com.uth.mobileBE.repositories.UserRepository;
//import com.uth.mobileBE.services.LoanService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/loans")
//public class LoanController {
//
//    @Autowired private LoanService loanService;
//
//    // Thay đổi đường dẫn: Xóa {libraryId} đi vì không cần client gửi lên nữa
//    @GetMapping("/filter")
//    public ResponseEntity<List<LoanResponse>> getFilteredLoans(
//
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
//            @RequestParam(required = false) String search) {
//        Long currentLibraryId=SecurityUtils.getLibraryId();
//
//        List<LoanResponse> results = loanService.getLoansWithFilter(
//                currentLibraryId, status, fromDate, toDate, search
//        );
//
//        return ResponseEntity.ok(results);
//    }
//
//
//
//    @GetMapping("/{id}")
//    public ResponseEntity<LoanResponse> getById(@PathVariable Long id) {
//        return ResponseEntity.ok(loanService.getLoanById(id));
//    }
//
//    @PostMapping
//    public ResponseEntity<LoanResponse> create(@RequestBody LoanRequest request) {
//        return new ResponseEntity<>(loanService.createLoan(request), HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<LoanResponse> update(@PathVariable Long id, @RequestBody LoanRequest request) {
//        return ResponseEntity.ok(loanService.updateLoan(id, request));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        loanService.deleteLoan(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/borrowing/count")
//    public ResponseEntity<Long> countBorrowingLoans() {
//        Long libraryId = SecurityUtils.getLibraryId();
//        return ResponseEntity.ok(loanService.countBorrowingLoans(libraryId));
//    }
//
//    @GetMapping("/overdue/count")
//    public ResponseEntity<Long> countOverdueLoans() {
//        Long libraryId = SecurityUtils.getLibraryId();
//        return ResponseEntity.ok(loanService.countOverdueLoans(libraryId));
//    }
//}