package com.uth.mobileBE.dto.response;

import com.uth.mobileBE.models.enums.PaymentMethod;
import com.uth.mobileBE.models.enums.StatusFeeInvoice;
import com.uth.mobileBE.models.enums.TypeFeeInvoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeInvoiceResponse {
    private Long invoiceId;
    private Long libraryId;
    private Long readerId;
    private String readerName;
    private Long loanDetailId;

    private TypeFeeInvoice type;
    private PaymentMethod paymentMethod;
    private Double totalAmount;
    private StatusFeeInvoice status;

    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
