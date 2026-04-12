package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.FeeConfig;
import com.uth.mobileBE.models.enums.TypeFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeConfigRepository extends JpaRepository<FeeConfig, Long> {

    // Lấy tất cả cấu hình phí của một thư viện
    List<FeeConfig> findByLibraryId(Long libraryId);

    // Lấy 1 cấu hình phí cụ thể (Dùng để tính tiền mượn/phạt sau này)
    Optional<FeeConfig> findByLibrary_LibraryIdAndFeeType(Long libraryId, TypeFeeConfig feeType);

    // Kiểm tra xem loại phí này đã được set cho thư viện chưa
    boolean existsByLibrary_LibraryIdAndFeeType(Long libraryId, TypeFeeConfig feeType);
}