package com.uth.mobileBE.dto.request;

import com.uth.mobileBE.models.enums.StatusLoanDetail;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LoanDetailRequest {
    private Long loanId;
    private Long copyId;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private StatusLoanDetail status;
    private Double penaltyAmount;
}