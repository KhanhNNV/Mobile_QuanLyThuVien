package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Book;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByLibrary_LibraryId(Long libraryId);

    boolean existsByIsbnAndLibrary_LibraryId(String isbn, Long libraryId);

    boolean existsByIsbnAndLibrary_LibraryIdAndBookIdNot(String isbn, Long libraryId, Long bookId);

    Long countByLibrary_LibraryId(Long libraryId);

    @Query("SELECT b.title FROM Book b WHERE b.library.libraryId = :libraryId AND " +
            "(SELECT COUNT(c) FROM BookCopy c WHERE c.book = b AND c.status = 'AVAILABLE') < 2")
    List<String> findBooksWithLowAvailableCopies(@Param("libraryId") Long libraryId);
}
