package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    @Query("SELECT bc FROM BookCopy bc WHERE bc.book.bookId = :bookId")
    List<BookCopy> findByBookId(@Param("bookId") Long bookId);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndCopyIdNot(String barcode, Long copyId);
}
