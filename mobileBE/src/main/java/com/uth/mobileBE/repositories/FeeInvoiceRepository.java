package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.FeeInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, Long> {
    List<FeeInvoice> findByLibrary_LibraryId(Long libraryId);

    //Tìm kiếm hóa đơn của phiếu vi phạm theo LoanDetailId
    Optional<FeeInvoice> findFirstByLoanDetail_LoanDetailIdOrderByCreatedAtDesc(Long loanDetailId);
}