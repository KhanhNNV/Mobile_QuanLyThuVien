package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Book;
import com.uth.mobileBE.models.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>{
}
