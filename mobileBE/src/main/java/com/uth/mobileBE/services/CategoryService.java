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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final LibraryRepository libraryRepository;

    @Transactional
    public CategoryResponse createFirstCategory(CategoryRequest request) {
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
    // 1. TẠO THỂ LOẠI MỚI
    public CategoryResponse createCategory(CategoryRequest request) {
        // Kiểm tra xem Library có tồn tại không
        Library library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện với ID: " + request.getLibraryId()));

        // Kiểm tra trùng tên thể loại trong cùng 1 thư viện
        if (categoryRepository.existsByNameAndLibrary_LibraryId(request.getName(), library.getLibraryId())) {
            throw new RuntimeException("Tên thể loại đã tồn tại trong thư viện này!");
        }

        Category category = Category.builder()
                .library(library)
                .name(request.getName())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    // 2. LẤY TẤT CẢ THỂ LOẠI CỦA 1 THƯ VIỆN
    public List<CategoryResponse> getAllCategoriesByLibrary(Long libraryId) {
        List<Category> categories = categoryRepository.findByLibrary_LibraryId(libraryId);
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. CẬP NHẬT THỂ LOẠI
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại với ID: " + categoryId));

        category.setName(request.getName());

        Category updatedCategory = categoryRepository.save(category);
        return mapToResponse(updatedCategory);
    }

    // 4. XÓA THỂ LOẠI
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Không tìm thấy thể loại với ID: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }

    // Hàm phụ trợ map từ Entity sang DTO
    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                // Dùng toán tử 3 ngôi: Nếu Library khác null thì lấy ID, ngược lại thì để null
                .libraryId(category.getLibrary() != null ? category.getLibrary().getLibraryId() : null)
                .build();
    }
}
