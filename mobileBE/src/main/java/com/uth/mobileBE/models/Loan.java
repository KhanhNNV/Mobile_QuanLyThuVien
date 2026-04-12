package com.uth.mobileBE.models;

import com.uth.mobileBE.models.enums.StatusLoan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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

    private LocalDateTime borrowDate;

    @Enumerated(EnumType.STRING)
    private StatusLoan status;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL)
    private List<LoanDetail> loanDetails;

    @CreationTimestamp
    @Column(updatable = false) // Không cho phép sửa ngày tạo sau khi đã tạo
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;
}
