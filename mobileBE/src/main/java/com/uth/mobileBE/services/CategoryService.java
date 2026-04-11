package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.CategoryRequest;
import com.uth.mobileBE.dto.response.CategoryResponse;
import com.uth.mobileBE.models.Category;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.repositories.CategoryRepository;
import com.uth.mobileBE.repositories.LibraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final LibraryRepository libraryRepository;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Library library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện"));

        if (categoryRepository.existsByNameAndLibrary_LibraryId(request.getName(), request.getLibraryId())) {
            throw new RuntimeException("Tên danh mục đã tồn tại trong thư viện này");
        }

        Category category = Category.builder()
                .name(request.getName())
                .library(library)
                .build();

        Category savedCategory = categoryRepository.save(category);


        return CategoryResponse.builder()
                .categoryId(savedCategory.getCategoryId())
                .name(savedCategory.getName())
                .libraryId(library.getLibraryId())
                .build();
    }
}
