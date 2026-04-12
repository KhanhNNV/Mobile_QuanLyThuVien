package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByLibrary_LibraryId(Long libraryId);

    boolean existsByIsbnAndLibrary_LibraryId(String isbn, Long libraryId);

    boolean existsByIsbnAndLibrary_LibraryIdAndBookIdNot(String isbn, Long libraryId, Long bookId);
}
