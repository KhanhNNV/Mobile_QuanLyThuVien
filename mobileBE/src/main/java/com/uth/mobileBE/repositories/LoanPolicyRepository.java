package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.LoanPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanPolicyRepository extends JpaRepository<LoanPolicy, Long> {
}
