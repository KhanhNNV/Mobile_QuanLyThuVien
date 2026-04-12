package com.uth.mobileBE.dto.request;

import com.uth.mobileBE.models.enums.StatusFeeInvoice;
import com.uth.mobileBE.models.enums.TypeFeeInvoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeInvoiceRequest {
    private Long libraryId;  // Bắt buộc
    private Long readerId;   // Bắt buộc
    private Long loanId;     // Có thể null

    private TypeFeeInvoice type;
    private Double totalAmount;
    private StatusFeeInvoice status;
}
