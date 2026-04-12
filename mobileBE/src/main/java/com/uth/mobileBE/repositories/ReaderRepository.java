package com.uth.mobileBE.repositories;

import com.uth.mobileBE.models.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {
    // Spring Boot sẽ tự động cung cấp các hàm:
    // save(), findById(), findAll(), deleteById(), count()...

    // Nếu sau này bạn muốn tìm kiếm người đọc theo số điện thoại hoặc email,
    // bạn có thể thêm các hàm như:
    // Optional<Reader> findByPhoneNumber(String phoneNumber);
    // List<Reader> findByFullNameContaining(String name);
}