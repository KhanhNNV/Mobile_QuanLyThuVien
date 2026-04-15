//package com.uth.mobileBE.models;
//
//import com.uth.mobileBE.models.enums.StatusLoanDetail;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Entity
//@Table(name = "loan_detail")
//public class LoanDetail {
//
//    @EmbeddedId
//    private LoanDetailId id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("loanId")
//    @JoinColumn(name = "loan_id")
//    private Loan loan;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("copyId")
//    @JoinColumn(name = "copy_id")
//    private BookCopy bookCopy;
//
//    private LocalDateTime dueDate;
//
//    private LocalDateTime returnDate;
//
//    @Enumerated(EnumType.STRING)
//    private StatusLoanDetail status;
//
//    private Double penaltyAmount;
//    @CreationTimestamp
//    @Column(updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    private LocalDateTime updateAt;
//}
