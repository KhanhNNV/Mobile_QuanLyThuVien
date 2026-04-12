package com.uth.mobileBE.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    private String name;
    // Tự động lấy giờ hệ thống lúc mới tạo
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Tự động cập nhật giờ mỗi khi có thay đổi (update)
    @UpdateTimestamp
    private LocalDateTime updateAt;

}