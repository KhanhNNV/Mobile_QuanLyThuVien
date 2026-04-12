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
    private Long libraryId;  // Bắt buộc
    private Long categoryId; // Có thể null (áp dụng chung cho toàn thư viện)

    private Boolean applyForStudent;
    private Integer maxBorrowDays;
}
