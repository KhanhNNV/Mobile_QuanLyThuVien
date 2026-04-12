package com.uth.mobileBE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReaderRequest {
    private String fullName;
    private String phone;
    private String barcode;
    private Boolean isStudent;
    private Long libraryId;
    private LocalDateTime membershipExpiry;
}