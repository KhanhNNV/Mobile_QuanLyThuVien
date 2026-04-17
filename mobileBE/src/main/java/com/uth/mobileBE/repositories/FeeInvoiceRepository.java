package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.FeeInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, Long> {
    List<FeeInvoice> findByLibrary_LibraryId(Long libraryId);
}