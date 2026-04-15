package com.uth.mobileBE.dto.response;

import com.uth.mobileBE.models.enums.StatusLoanDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailInfoDto {
    private Long copyId;         // RẤT QUAN TRỌNG: Dùng để gọi API Sửa/Xóa từng cuốn
    private String title;
    private String author;
    private String category;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private StatusLoanDetail status; // Hoặc String nếu bạn đã đổi sang String
}