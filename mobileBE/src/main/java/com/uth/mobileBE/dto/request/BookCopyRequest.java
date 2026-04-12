package com.uth.mobileBE.dto.request;

import lombok.Data;

@Data
public class BookCopyRequest {
    private Long bookId;
    private String barcode;
    /** NEW, GOOD, FAIR, POOR — khi tạo có thể bỏ trống (mặc định NEW) */
    private String condition;
    /** AVAILABLE, BORROWED, LOST, DAMAGED — khi tạo có thể bỏ trống (mặc định AVAILABLE) */
    private String status;
}
