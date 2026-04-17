package com.uth.mobileBE.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uth.mobileBE.models.enums.StatusViolation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationResponse {
    private Long violationId;
    private Long readerId;
    private String readerName;
    private String barcode;

    private String reason;
    private String status; // Nên dùng Enum thay vì String

    // Thêm thông tin sách để người dùng biết vi phạm liên quan đến sách nào
    private Long bookId;
    private String bookTitle;

    // Thêm thông tin mượn sách
    private Long loanId;
    private Long loanDetailId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}