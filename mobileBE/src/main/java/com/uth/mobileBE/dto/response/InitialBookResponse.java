package com.uth.mobileBE.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InitialBookResponse {
    private Long bookId;
    private String title;
    private String author;
    private String isbn;
    private Double basePrice;

    private Long libraryId;
    private Long categoryId;

    // thông tin copy đầu tiên
    private Long copyId;
    private String barcode;
    private String condition;
    private String status;
}
