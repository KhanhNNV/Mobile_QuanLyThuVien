package com.uth.mobileBE.dto.response;

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

    // Trả về ID của các entity liên quan
    private Long libraryId;
    private Long readerId;
    private Long loanId;

    private TypeFeeInvoice type;
    private Double totalAmount;
    private StatusFeeInvoice status;

    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
