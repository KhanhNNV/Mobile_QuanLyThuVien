package com.uth.mobileBE.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPolicyResponse {
    private Long policyId;

    private Long libraryId;
    private Long categoryId;

    private Boolean applyForStudent;
    private Integer maxBorrowDays;

    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
