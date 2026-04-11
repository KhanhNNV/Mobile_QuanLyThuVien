package com.uth.mobileBE.repository;

import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Long> {
    Optional<Library> findByLibraryId(Long id);
}
