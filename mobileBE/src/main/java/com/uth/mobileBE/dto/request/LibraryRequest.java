package com.uth.mobileBE.dto.request;

import com.uth.mobileBE.models.enums.StatusLibrary;
import jakarta.persistence.criteria.CriteriaBuilder;
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
    private Long platformFeeExpiry;
    private StatusLibrary status;
}
