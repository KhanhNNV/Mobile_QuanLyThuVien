package com.uth.mobileBE.dto.request;

import lombok.Data;

@Data
public class BookRequest {
    private Long categoryId;
    private String isbn;
    private String title;
    private String author;
    private Double basePrice;
}
