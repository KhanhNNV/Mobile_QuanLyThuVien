package com.uth.mobileBE.dto.request;

import com.uth.mobileBE.models.enums.TypeFeeConfig;
import lombok.Data;

@Data
public class FeeConfigRequest {
    private Long libraryId;
    private TypeFeeConfig feeType;
    private Double amount;
}