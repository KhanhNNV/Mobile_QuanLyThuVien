package com.uth.mobileBE.dto.request;

import com.uth.mobileBE.models.enums.StatusLibrary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryRequest {
    private String name;
    private String address;
    private Boolean hasStudentDiscount;
    private Long platformFeeExpiry;
    private StatusLibrary status;
}
