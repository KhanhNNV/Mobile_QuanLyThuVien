package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.LoanPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanPolicyRepository extends JpaRepository<LoanPolicy, Long> {
    List<LoanPolicy> findByLibrary_LibraryId(Long libraryId);
}
