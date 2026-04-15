package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.BookCopy;
import com.uth.mobileBE.models.enums.StatusBookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    List<BookCopy> findByBook_BookId(Long bookId);

    boolean existsByBarcode(String barcode);

    // Tìm sách theo thư viện và trạng thái
    List<BookCopy> findByBook_Library_LibraryIdAndStatus(Long libraryId, StatusBookCopy status);
}
