package com.uth.mobileBE.models;

import com.uth.mobileBE.models.enums.TypeFeeConfig;
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
@Table(name = "fee_config")
public class FeeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long configId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @Enumerated(EnumType.STRING)
    private TypeFeeConfig feeType;

    private Double amount;
    // Tự động lấy giờ hệ thống lúc mới tạo
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Tự động cập nhật giờ mỗi khi có thay đổi (update)
    @UpdateTimestamp
    private LocalDateTime updateAt;

}