package com.uth.mobileBE.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reader")
public class Reader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long readerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    private String fullName;
    private String phone;

    @Column(unique = true, nullable = false)
    private String barcode;

    private Boolean isStudent;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime membershipExpiry;

    private Boolean isBlocked;
}