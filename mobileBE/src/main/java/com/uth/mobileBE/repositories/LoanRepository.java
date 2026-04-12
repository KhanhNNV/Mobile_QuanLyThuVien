package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    // Bạn có thể thêm các hàm tìm kiếm nhanh tại đây, ví dụ:
    // List<Loan> findByReader_ReaderId(Long readerId);
}