package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.FeeInvoice;
import com.uth.mobileBE.models.Reader;
import com.uth.mobileBE.models.enums.StatusFeeInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, Long> {
    List<FeeInvoice> findByLibrary_LibraryId(Long libraryId);
  
   //Tìm kiếm hóa đơn của phiếu vi phạm theo LoanDetailId
    Optional<FeeInvoice> findFirstByLoanDetail_LoanDetailIdOrderByCreatedAtDesc(Long loanDetailId);

    //Tổng tiền nợ dự vào readerId và Status
    @Query("SELECT SUM(f.totalAmount) FROM FeeInvoice f " +
            "WHERE f.reader.readerId = :readerId AND f.status = :status")
    BigDecimal sumTotalAmountByReaderIdAndStatus(@Param("readerId") Long readerId,
                                                 @Param("status") StatusFeeInvoice status);

    Long reader(Reader reader);

    @Query("SELECT f FROM FeeInvoice f WHERE f.library.libraryId = :libraryId AND " +
            "(:status IS NULL OR f.status = :status) AND " + // Thêm dòng này
            "(:keyword IS NULL OR :keyword = '' OR " +
            "f.invoiceId = :searchId OR " +
            "LOWER(f.reader.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<FeeInvoice> searchInvoicesByLibrary(
            @Param("libraryId") Long libraryId,
            @Param("status") StatusFeeInvoice status,
            @Param("keyword") String keyword,
            @Param("searchId") Long searchId,
            Pageable pageable
    );
}