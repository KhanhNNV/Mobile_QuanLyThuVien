package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.User;
import com.uth.mobileBE.models.enums.Role;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    List<User> findByLibrary_LibraryId(Long libraryId);

    List<User> findByLibraryAndIsActive(Library library, Boolean isActive);

    List<User> findByLibraryAndRole(Library library, Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.library = :library AND u.role = :role AND u.isActive = true")
    long countActiveAdminsByLibrary(@Param("library") Library library, @Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.library.libraryId = :libraryId")
    List<User> findByLibraryId(@Param("libraryId") Long libraryId);

    @Query("SELECT u FROM User u WHERE u.library.libraryId = :libraryId AND u.isActive = :isActive")
    List<User> findByLibraryIdAndIsActive(@Param("libraryId") Long libraryId, @Param("isActive") Boolean isActive);
}
