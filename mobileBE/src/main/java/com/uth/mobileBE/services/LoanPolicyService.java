package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.LoanPolicyRequest;
import com.uth.mobileBE.dto.response.LoanPolicyResponse;
import com.uth.mobileBE.models.Category;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.LoanPolicy;
import com.uth.mobileBE.repositories.CategoryRepository;
import com.uth.mobileBE.repositories.LibraryRepository;
import com.uth.mobileBE.repositories.LoanPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanPolicyService {

    @Autowired
    private LoanPolicyRepository loanPolicyRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public LoanPolicyResponse createLoanPolicy(LoanPolicyRequest request) {
        // Kiểm tra Library (bắt buộc)
        Library library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Library với id: " + request.getLibraryId()));

        // Kiểm tra Category (có thể null)
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Category với id: " + request.getCategoryId()));
        }

        LocalDateTime now = LocalDateTime.now();

        LoanPolicy policy = LoanPolicy.builder()
                .library(library)
                .category(category)
                .applyForStudent(request.getApplyForStudent())
                .maxBorrowDays(request.getMaxBorrowDays())
                .createdAt(now)
                .updateAt(now)
                .build();

        LoanPolicy savedPolicy = loanPolicyRepository.save(policy);
        return mapToResponse(savedPolicy);
    }

    public List<LoanPolicyResponse> getAllLoanPolicies() {
        return loanPolicyRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LoanPolicyResponse getLoanPolicyById(Long id) {
        LoanPolicy policy = getLoanPolicyEntityById(id);
        return mapToResponse(policy);
    }

    public LoanPolicyResponse updateLoanPolicy(Long id, LoanPolicyRequest request) {
        LoanPolicy existingPolicy = getLoanPolicyEntityById(id);

        // Cập nhật thông tin cơ bản
        if (request.getApplyForStudent() != null) {
            existingPolicy.setApplyForStudent(request.getApplyForStudent());
        }
        if (request.getMaxBorrowDays() != null) {
            existingPolicy.setMaxBorrowDays(request.getMaxBorrowDays());
        }

        // Cập nhật Library nếu có thay đổi
        if (request.getLibraryId() != null) {
            Library library = libraryRepository.findById(request.getLibraryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Library"));
            existingPolicy.setLibrary(library);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Category"));
            existingPolicy.setCategory(category);
        }

        existingPolicy.setUpdateAt(LocalDateTime.now());

        LoanPolicy updatedPolicy = loanPolicyRepository.save(existingPolicy);
        return mapToResponse(updatedPolicy);
    }

    public void deleteLoanPolicy(Long id) {
        LoanPolicy policy = getLoanPolicyEntityById(id);
        loanPolicyRepository.delete(policy);
    }

    // --- CÁC HÀM HỖ TRỢ NỘI BỘ ---

    private LoanPolicy getLoanPolicyEntityById(Long id) {
        return loanPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy LoanPolicy với id: " + id));
    }

    private LoanPolicyResponse mapToResponse(LoanPolicy policy) {
        return LoanPolicyResponse.builder()
                .policyId(policy.getPolicyId())
                .libraryId(policy.getLibrary().getLibraryId())
                .categoryId(policy.getCategory() != null ? policy.getCategory().getCategoryId() : null) // Giả sử Model có getCategoryId()
                .applyForStudent(policy.getApplyForStudent())
                .maxBorrowDays(policy.getMaxBorrowDays())
                .createdAt(policy.getCreatedAt())
                .updateAt(policy.getUpdateAt())
                .build();
    }
}
