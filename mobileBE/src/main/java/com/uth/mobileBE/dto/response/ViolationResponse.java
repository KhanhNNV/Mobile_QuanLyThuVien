package com.uth.mobileBE.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationResponse {
    private Long violationId;
    private Long readerId;
    private String readerName;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}