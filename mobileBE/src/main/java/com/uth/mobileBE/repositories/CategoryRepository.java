package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameAndLibrary_LibraryId(String name, Long libraryId);
}
