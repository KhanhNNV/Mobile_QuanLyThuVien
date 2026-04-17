package com.uth.mobileBE.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateLoanDetailRequest {
    @NotNull(message = "Mã bản sao sách không được để trống")
    private Long copyId;

    @NotNull(message = "Trạng thái không được để trống")
    private String status; // BORROWING, RETURNED, LOST

    private LocalDateTime dueDate;

    private String condition;
}