package com.uth.mobileBE.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanDetailRequest {

    @NotNull(message = "Loan ID không được để trống")
    private Long loanId;

    @NotNull(message = "Mã bản sao sách (Copy ID) không được để trống")
    private Long copyId;

    @NotNull(message = "Số ngày mượn không được để trống")
    @Min(value = 1, message = "Số ngày mượn ít nhất phải là 1")
    private Integer borrowDays;

}