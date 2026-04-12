package com.uth.mobileBE.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uth.mobileBE.models.enums.StatusLoan;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LoanResponse {
    private Long loanId;
    private String libraryName;
    private String readerName;
    private String processorName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime borrowDate;

    private StatusLoan status;
    private List<String> bookTitles;

    // THÊM 2 TRƯỜNG NÀY VÀO:
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;
}