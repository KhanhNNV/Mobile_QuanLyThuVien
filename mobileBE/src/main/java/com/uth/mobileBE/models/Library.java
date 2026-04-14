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
@Table(name = "librarys")
public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long libraryId;

    private String name;
    private String address;
    private Boolean hasStudentDiscount;
    private Long platformFeeExpiry;

    @Enumerated(EnumType.STRING)
    private StatusLibrary status = StatusLibrary.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;
}
