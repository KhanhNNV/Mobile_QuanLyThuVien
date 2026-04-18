package com.uth.mobileBE.dto.request;

import com.uth.mobileBE.models.enums.StatusViolation;
import lombok.Data;

@Data
public class UpdateViolationRequest {
    private String reason;
    private StatusViolation status;
}