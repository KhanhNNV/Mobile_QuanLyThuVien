package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViolationRepository extends JpaRepository<Violation,Long> {
    List<Violation> findByReaderReaderId(Long readerId);

    Optional<Violation> findByLoan_LoanId(Long loanId);
}
