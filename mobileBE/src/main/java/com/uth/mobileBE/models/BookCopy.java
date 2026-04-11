package com.uth.mobileBE.models;

import com.uth.mobileBE.models.enums.ConditionBookCopy;
import com.uth.mobileBE.models.enums.StatusBookCopy;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "book_copy")
public class BookCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long copyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(unique = true, nullable = false)
    private String barcode;

    private ConditionBookCopy condition; // NEW/GOOD/FAIR/POOR

    private StatusBookCopy status; // AVAILABLE/BORROWED/LOST/DAMAGED
}