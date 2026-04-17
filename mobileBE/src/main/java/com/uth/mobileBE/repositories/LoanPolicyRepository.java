package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.LoanPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanPolicyRepository extends JpaRepository<LoanPolicy, Long> {
    List<LoanPolicy> findByLibrary_LibraryId(Long libraryId);

    // Tìm policy theo library và category cụ thể
    Optional<LoanPolicy> findByLibrary_LibraryIdAndCategory_CategoryId(Long libraryId, Long categoryId);

    // Tìm policy mặc định (áp dụng chung cho thư viện khi category = null)
    Optional<LoanPolicy> findByLibrary_LibraryIdAndCategoryIsNull(Long libraryId);
}
