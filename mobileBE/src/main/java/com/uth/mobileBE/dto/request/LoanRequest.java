package com.uth.mobileBE.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanRequest {

    @NotNull(message = "Library ID không được để trống")
    private Long libraryId;

    @NotNull(message = "Reader ID không được để trống")
    private Long readerId;

    @NotNull(message = "ID Nhân viên xử lý không được để trống")
    private Long processedBy;

}