package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.FeeInvoice;
import com.uth.mobileBE.models.Reader;
import com.uth.mobileBE.models.enums.StatusFeeInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, Long> {
    List<FeeInvoice> findByLibrary_LibraryId(Long libraryId);

    //Tổng tiền nợ dự vào readerId và Status
    @Query("SELECT SUM(f.totalAmount) FROM FeeInvoice f " +
            "WHERE f.reader.readerId = :readerId AND f.status = :status")
    BigDecimal sumTotalAmountByReaderIdAndStatus(@Param("readerId") Long readerId,
                                                 @Param("status") StatusFeeInvoice status);

    Long reader(Reader reader);
}