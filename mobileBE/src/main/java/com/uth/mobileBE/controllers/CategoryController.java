package com.uth.mobileBE.controllers;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.CategoryRequest;
import com.uth.mobileBE.dto.response.CategoryResponse;
import com.uth.mobileBE.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    // API: POST /api/categories
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        CategoryResponse createdCategory = categoryService.createCategory(request);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    // API: GET /api/categories
    @GetMapping()
    public ResponseEntity<List<CategoryResponse>> getCategoriesByLibrary() {
        Long libraryId = SecurityUtils.getLibraryId();
        List<CategoryResponse> categories = categoryService.getAllCategoriesByLibrary(libraryId);
        return ResponseEntity.ok(categories);
    }

    // API: PUT /api/categories/{categoryId}
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryRequest request) {
        CategoryResponse updatedCategory = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(updatedCategory);
    }

    // API: DELETE /api/categories/{categoryId}
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Xóa thể loại thành công!");
    }
}