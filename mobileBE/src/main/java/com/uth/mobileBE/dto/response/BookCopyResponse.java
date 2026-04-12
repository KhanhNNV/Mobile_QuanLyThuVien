package com.uth.mobileBE.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookCopyResponse {
    private Long copyId;
    private Long bookId;
    private String barcode;
    private String condition;
    private String status;
}
