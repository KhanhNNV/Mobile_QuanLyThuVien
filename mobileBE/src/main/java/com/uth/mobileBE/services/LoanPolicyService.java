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
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public List<LoanPolicyResponse> getPoliciesByLibrary(Long libraryId) {
        return loanPolicyRepository.findByLibrary_LibraryId(libraryId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public LoanPolicyResponse createPolicy(Long libraryId, LoanPolicyRequest request) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));
        }

        LoanPolicy policy = LoanPolicy.builder()
                .library(library)
                .category(category)
                .maxBorrowDays(request.getMaxBorrowDays())
                .build();

        return mapToResponse(loanPolicyRepository.save(policy));
    }

    @Transactional
    public LoanPolicyResponse updatePolicy(Long policyId, Long libraryId, LoanPolicyRequest request) {
        LoanPolicy policy = loanPolicyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chính sách"));

        if (!policy.getLibrary().getLibraryId().equals(libraryId)) {
            throw new RuntimeException("Không có quyền chỉnh sửa chính sách này");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));
        }

        policy.setCategory(category);
        policy.setMaxBorrowDays(request.getMaxBorrowDays());

        return mapToResponse(loanPolicyRepository.save(policy));
    }

    public void deletePolicy(Long policyId, Long libraryId) {
        LoanPolicy policy = loanPolicyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chính sách"));

        if (!policy.getLibrary().getLibraryId().equals(libraryId)) {
            throw new RuntimeException("Không có quyền xóa chính sách này");
        }
        loanPolicyRepository.delete(policy);
    }

    private LoanPolicyResponse mapToResponse(LoanPolicy policy) {
        return LoanPolicyResponse.builder()
                .policyId(policy.getPolicyId())
                .categoryId(policy.getCategory() != null ? policy.getCategory().getCategoryId() : null)
                .categoryName(policy.getCategory() != null ? policy.getCategory().getName() : "Tất cả thể loại")
                .maxBorrowDays(policy.getMaxBorrowDays())
                .build();
    }
}
