package com.uth.mobileBE.dto.response;

import com.uth.mobileBE.models.enums.ConditionBookCopy;
import com.uth.mobileBE.models.enums.StatusBookCopy;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookCopyResponse {
    private Long copyId;
    private Long bookId;
    private String barcode;
    private ConditionBookCopy condition;
    private StatusBookCopy status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
