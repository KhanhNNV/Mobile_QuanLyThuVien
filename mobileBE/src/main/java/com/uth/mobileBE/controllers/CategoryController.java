package com.uth.mobileBE.controllers;

import com.uth.mobileBE.dto.request.CategoryRequest;
import com.uth.mobileBE.dto.response.CategoryResponse;
import com.uth.mobileBE.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/welcome")
    public ResponseEntity<CategoryResponse> createFirstCategory(@RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createFirstCategory(request);
        return ResponseEntity.ok(response);
    }
}