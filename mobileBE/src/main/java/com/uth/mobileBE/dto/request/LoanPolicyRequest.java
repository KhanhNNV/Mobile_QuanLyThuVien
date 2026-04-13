package com.uth.mobileBE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPolicyRequest {
    private Long categoryId;
    private Boolean applyForStudent;
    private Integer maxBorrowDays;
}
