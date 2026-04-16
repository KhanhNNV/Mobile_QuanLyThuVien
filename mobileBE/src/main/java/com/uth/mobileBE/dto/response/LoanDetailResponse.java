package com.uth.mobileBE.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uth.mobileBE.models.enums.StatusLoanDetail;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanDetailResponse {

    // BỔ SUNG: ID của chính chi tiết mượn này (Rất quan trọng để gọi API trả sách)
    private Long loanDetailId;

    private Long loanId;
    private Long copyId;

    // Thông tin kèm theo để hiển thị lên UI cho đẹp mà không cần gọi API khác
    private Long bookId;
    private String bookTitle;

    private String author;
    private String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnDate;

    private StatusLoanDetail status;
    private Double penaltyAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;
}