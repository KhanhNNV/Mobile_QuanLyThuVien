package com.uth.mobileBE.controllers;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.LoanRequest;
import com.uth.mobileBE.dto.response.LoanResponse;
import com.uth.mobileBE.models.User;
import com.uth.mobileBE.repositories.UserRepository;
import com.uth.mobileBE.services.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired private LoanService loanService;
    @Autowired
    private UserRepository userRepository;

    // Thay đổi đường dẫn: Xóa {libraryId} đi vì không cần client gửi lên nữa
    @GetMapping("/filter")
    public ResponseEntity<List<LoanResponse>> getFilteredLoans(
            Authentication authentication, // Dùng Authentication thay vì @AuthenticationPrincipal User
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String search) {

        // 1. Kiểm tra xem request có token hợp lệ không
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized
        }

        // 2. Lấy username từ Authentication (do file Filter của bạn đang lưu dạng chuỗi)
        String username = authentication.getName();

        // In ra để bạn debug chắc chắn token đã nhận đúng username
        System.out.println("Username đang gửi request: " + username);

        // 3. Fetch user từ DB dựa vào username
        // (Đảm bảo UserRepository của bạn đã có hàm findByUsername)
        User userFromDb = userRepository.findByUsername(username)
                .orElse(null);

        // Nếu token có username nhưng dưới DB đã bị xóa
        if (userFromDb == null) {
            return ResponseEntity.status(401).build();
        }

        // 4. Kiểm tra quyền truy cập thư viện
        if (userFromDb.getLibrary() == null) {
            return ResponseEntity.status(403).build(); // 403 Forbidden
        }

        // 5. Trích xuất libraryId an toàn
        Long currentLibraryId = userFromDb.getLibrary().getLibraryId();

        // 6. Gọi Service với libraryId vừa lấy được
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

    @GetMapping("/borrowing/count")
    public ResponseEntity<Long> countBorrowingLoans() {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(loanService.countBorrowingLoans(libraryId));
    }

    @GetMapping("/overdue/count")
    public ResponseEntity<Long> countOverdueLoans() {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(loanService.countOverdueLoans(libraryId));
    }
}