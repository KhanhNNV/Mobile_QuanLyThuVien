package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.LoanDetail;
import com.uth.mobileBE.models.LoanDetailId;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanDetailRepository extends JpaRepository<LoanDetail, LoanDetailId> {
    // Lọc theo thư viện, trạng thái BORROWING và thời gian dueDate nằm trong ngày hôm nay
    // Dùng JOIN FETCH để gom luôn dữ liệu của bookCopy và book trong 1 lần query tránh lỗi LazyInitializationException do FetchType.LAZY ở các mqh
    @Query("SELECT ld FROM LoanDetail ld " +
            "JOIN ld.loan l " +
            "JOIN FETCH ld.bookCopy bc " +
            "JOIN FETCH bc.book b " +
            "WHERE l.library.libraryId = :libraryId " +
            "AND ld.status = 'BORROWING' " +
            "AND ld.dueDate >= :startOfDay AND ld.dueDate <= :endOfDay")
    List<LoanDetail> findDueToday(@Param("libraryId") Long libraryId,
                                  @Param("startOfDay") LocalDateTime startOfDay,
                                  @Param("endOfDay") LocalDateTime endOfDay);

    List<LoanDetail> findByLoan_LoanId(Long loanId);
}