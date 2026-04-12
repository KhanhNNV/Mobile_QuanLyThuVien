package com.uth.mobileBE.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookResponse {
    private Long bookId;
    private String isbn;
    private String title;
    private String author;
    private Double basePrice;
    private Long libraryId;
    private Long categoryId;
}
