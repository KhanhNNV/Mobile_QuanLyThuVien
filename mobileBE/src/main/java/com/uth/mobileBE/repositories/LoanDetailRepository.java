package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.LoanDetail;
import com.uth.mobileBE.models.LoanDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanDetailRepository extends JpaRepository<LoanDetail, LoanDetailId> {
}