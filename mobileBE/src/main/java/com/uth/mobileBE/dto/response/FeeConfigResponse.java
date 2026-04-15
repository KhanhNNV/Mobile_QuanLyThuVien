package com.uth.mobileBE.dto.response;

import com.uth.mobileBE.models.enums.TypeFeeConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeConfigResponse {
    private Long configId;
    private TypeFeeConfig feeType;
    private Double amount;
}
