package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibraryRepository extends JpaRepository<Library, Long> {
    Optional<Library> findByLibraryId(Long id);
}
