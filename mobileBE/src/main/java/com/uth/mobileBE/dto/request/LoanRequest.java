package com.uth.mobileBE.dto.request;

import com.uth.mobileBE.models.enums.StatusLoan;
import lombok.Data;

@Data
public class LoanRequest {
    private Long libraryId;
    private Long readerId;
    private Long processedBy;
    private StatusLoan status;
}