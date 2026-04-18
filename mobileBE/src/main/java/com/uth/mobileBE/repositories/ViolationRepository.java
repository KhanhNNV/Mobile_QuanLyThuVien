package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.LoanDetail;
import com.uth.mobileBE.models.Violation;
import com.uth.mobileBE.models.enums.StatusViolation;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ViolationRepository extends JpaRepository<Violation,Long> {
    List<Violation> findByReaderReaderId(Long readerId);

    Optional<Violation> findByLoan_LoanId(Long loanId);

    List<Violation> findByLoanDetailAndStatus(LoanDetail detail, StatusViolation status);

    List<Violation> findByReader_ReaderIdAndStatus(Long readerId, StatusViolation status);

    Long countByLibrary_LibraryIdAndStatus(Long libraryId, StatusViolation status);

    @Query("SELECT v FROM Violation v WHERE " +
            "v.library.libraryId = :libraryId AND " +
            "(:status IS NULL OR v.status = :status) AND " +
            "(:startDate IS NULL OR v.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR v.createdAt <= :endDate) AND " +
            "(:search IS NULL OR :search = '' OR v.violationId = :searchId OR LOWER(v.reader.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Violation> findViolationsWithFilters(
            @Param("libraryId") Long libraryId,
            @Param("status") StatusViolation status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("search") String search,
            @Param("searchId") Long searchId,
            Pageable pageable
    );
}
