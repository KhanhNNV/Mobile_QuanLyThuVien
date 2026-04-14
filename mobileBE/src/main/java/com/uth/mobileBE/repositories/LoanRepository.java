package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Loan;
import com.uth.mobileBE.models.enums.StatusLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {
    long countByLibrary_LibraryIdAndStatus(Long libraryId, StatusLoan status);
}