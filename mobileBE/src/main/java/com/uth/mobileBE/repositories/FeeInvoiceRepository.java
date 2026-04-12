package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.FeeInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, Long> {
}