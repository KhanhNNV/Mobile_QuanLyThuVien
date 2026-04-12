package com.uth.mobileBE.dto.request;

import lombok.Data;

@Data
public class BookCopyRequest {
    private Long bookId;
    private String barcode;
    private String condition;
    private String status;
}
