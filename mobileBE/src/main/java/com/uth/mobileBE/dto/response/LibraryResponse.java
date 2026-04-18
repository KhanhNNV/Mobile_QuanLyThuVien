package com.uth.mobileBE.dto.response;

import com.uth.mobileBE.models.enums.StatusLibrary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryResponse {
    private Long libraryId;
    private String name;
    private String address;
    private Boolean hasStudentDiscount;
    private Long platformFeeExpiry;
    private StatusLibrary status;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private Integer maxLoansQuota;
    private Integer maxBooksQuota;
}
