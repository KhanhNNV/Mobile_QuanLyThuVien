package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByLibrary_LibraryId(Long libraryId);
    boolean existsByNameAndLibrary_LibraryId(String name, Long libraryId);

}

