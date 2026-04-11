package com.uth.mobileBE.models;

import jakarta.persistence.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "library")
public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long libraryId;

    private String name;
    private String address;
    private Boolean hasStudentDiscount;
    private Long platformFeeExpiry;

    @Column(length = 20)
    private StatusLibrary status = StatusLibrary.ACTIVE;

    private Long createdAt;
}
