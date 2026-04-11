package com.uth.mobileBE.models;

import com.uth.mobileBE.models.enums.StatusFeeInvoice;
import com.uth.mobileBE.models.enums.TypeFeeInvoice;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fee_invoice")
public class FeeInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false)
    private Reader reader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id") // Nullable
    private Loan loan;

    @Enumerated(EnumType.STRING)
    private TypeFeeInvoice type;

    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private StatusFeeInvoice status;

    private Long createdAt;
}
