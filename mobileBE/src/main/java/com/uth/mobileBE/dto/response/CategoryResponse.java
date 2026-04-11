package com.uth.mobileBE.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long categoryId;
    private String name;
    private Long libraryId;
}
