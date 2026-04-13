package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {
    long countByLibrary_LibraryId(Long libraryId);

    // Tìm kiếm tương đối theo Tên, Số điện thoại hoặc Mã thẻ (Barcode)
    @Query("SELECT r FROM Reader r WHERE " +
            "LOWER(r.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "r.phone LIKE CONCAT('%', :query, '%') OR " +
            "LOWER(r.barcode) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Reader> searchReaders(@Param("query") String query);
}

