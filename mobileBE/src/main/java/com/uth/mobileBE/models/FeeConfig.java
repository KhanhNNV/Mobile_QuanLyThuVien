package com.uth.mobileBE.models;

import com.uth.mobileBE.models.enums.TypeFeeConfig;
import jakarta.persistence.*;
import lombok.*;

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
}