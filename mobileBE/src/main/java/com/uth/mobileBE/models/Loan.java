package com.uth.mobileBE.models;

import com.uth.mobileBE.models.enums.BookCondition;
import com.uth.mobileBE.models.enums.StatusLoan;
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
@Table(name = "loan")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false)
    private Reader reader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by", nullable = false)
    private User processedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copy_id", nullable = false)
    private BookCopy bookCopy;

    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;     // Hạn trả
    private LocalDateTime returnDate;  // Ngày trả thực tế

    @Enumerated(EnumType.STRING)
    private StatusLoan status;         // BORROWING, RETURNED, CLOSED, OVERDUE

    @Enumerated(EnumType.STRING)
    private BookCondition condition;   // NORMAL, LOST, DAMAGED

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;
}