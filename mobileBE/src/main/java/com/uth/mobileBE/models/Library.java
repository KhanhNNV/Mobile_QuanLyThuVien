package com.uth.mobileBE.models;

import com.uth.mobileBE.models.enums.StatusLibrary;
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
@Table(name = "libraries")
public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long libraryId;

    private String name;
    private String address;
    @Column(name = "max_loans_quota", nullable = false)
    private Integer maxLoansQuota=0;

    @Column(name = "max_books_quota", nullable = false)
    private Integer maxBooksQuota=0;

    @Enumerated(EnumType.STRING)
    private StatusLibrary status = StatusLibrary.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;
}
