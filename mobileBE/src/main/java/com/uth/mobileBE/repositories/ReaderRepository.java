package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Reader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {
    long countByLibrary_LibraryId(Long libraryId);
    // Tìm ID lớn nhất hiện tại. Dùng COALESCE để lỡ bảng chưa có ai thì trả về 0.
    @Query("SELECT COALESCE(MAX(r.readerId), 0) FROM Reader r")
    Long findMaxReaderId();

    // Kiểm tra barcode đã tồn tại chưa
    boolean existsByBarcode(String barcode);

    // Tìm kiếm tương đối theo Tên, Số điện thoại hoặc Mã thẻ (Barcode)
    @Query("SELECT r FROM Reader r WHERE r.library.libraryId = :libraryId AND (" +
            "LOWER(r.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "r.phone LIKE CONCAT('%', :query, '%') OR " +
            "LOWER(r.barcode) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Reader> searchReadersByLibraryId(@Param("libraryId") Long libraryId, @Param("query") String query);


    // Kiểm tra độc giả có tồn tại trong thư viện này không
    boolean existsByReaderIdAndLibrary_LibraryId(Long readerId, Long libraryId);

    /**
     * Tìm danh sách bạn đọc (Reader) theo ID thư viện với hỗ trợ phân trang.
     *
     * @param libraryId ID của thư viện (Library)
     * @param pageable  đối tượng chứa thông tin phân trang (số trang, kích thước, sắp xếp)
     * @return một trang (Page) chứa danh sách các Reader tương ứng với thư viện
     */
    Page<Reader> findByLibrary_LibraryId(Long libraryId, Pageable pageable);
}

