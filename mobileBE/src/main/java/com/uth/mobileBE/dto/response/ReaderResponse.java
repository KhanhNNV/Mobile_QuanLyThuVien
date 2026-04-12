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
public class ReaderResponse {
    private Long readerId;
    private String fullName;
    private String phone;
    private String barcode;
    private Boolean isStudent;
    private Boolean isBlocked;
    private LocalDateTime membershipExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}