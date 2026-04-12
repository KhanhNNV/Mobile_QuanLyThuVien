package com.uth.mobileBE.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookResponse {
    private Long bookId;
    private Long libraryId;
    private Long categoryId;
    private String isbn;
    private String title;
    private String author;
    private Double basePrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
