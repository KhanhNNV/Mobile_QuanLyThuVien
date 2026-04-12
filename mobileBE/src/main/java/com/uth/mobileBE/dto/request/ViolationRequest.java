package com.uth.mobileBE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationRequest {
    private Long readerId;
    private Long libraryId;
    private Long loanId; // Có thể null nếu vi phạm không liên quan lượt mượn cụ thể
    private String reason;
    private String status; // ACTIVE hoặc RESOLVED
}