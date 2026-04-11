package com.uth.mobileBE.dto.request;

import lombok.Data;

@Data
public class InitialBookRequest {
    private String title;
    private String author;
    private String isbn;
    private Double basePrice;
    private Long categoryId;
    private Long libraryId;
    private String barcode; // Barcode cho bản sao đầu tiên
}
