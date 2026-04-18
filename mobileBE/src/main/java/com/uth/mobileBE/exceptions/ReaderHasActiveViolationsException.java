package com.uth.mobileBE.exceptions;

import com.uth.mobileBE.dto.response.ViolationResponse;
import com.uth.mobileBE.models.Violation;
import lombok.Getter;

import java.util.List;

@Getter
public class ReaderHasActiveViolationsException extends RuntimeException {
    private final List<ViolationResponse> activeViolations;

    public ReaderHasActiveViolationsException(String message, List<ViolationResponse> activeViolations) {
        super(message);
        this.activeViolations = activeViolations;
    }
}