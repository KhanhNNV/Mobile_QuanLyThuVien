package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {
    long countByLibrary_LibraryId(Long libraryId);
    // Tìm ID lớn nhất hiện tại. Dùng COALESCE để lỡ bảng chưa có ai thì trả về 0.
    @Query("SELECT COALESCE(MAX(r.readerId), 0) FROM Reader r")
    Long findMaxReaderId();
}

