package com.uth.mobileBE.services;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.FeeConfigRequest;
import com.uth.mobileBE.dto.response.FeeConfigResponse;
import com.uth.mobileBE.models.FeeConfig;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.enums.TypeFeeConfig;
import com.uth.mobileBE.repositories.FeeConfigRepository;
import com.uth.mobileBE.repositories.LibraryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeeConfigService {

    private final FeeConfigRepository feeConfigRepository;
    private final LibraryRepository libraryRepository;

    // 1. TẠO CẤU HÌNH PHÍ MỚI
    public FeeConfigResponse createFeeConfig(FeeConfigRequest request,Long libraryId) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện với ID: " + libraryId));

        // Kiểm tra xem thư viện này đã cài đặt loại phí này chưa
        if (feeConfigRepository.existsByLibrary_LibraryIdAndFeeType(library.getLibraryId(), request.getFeeType())) {
            throw new RuntimeException("Loại phí " + request.getFeeType() + " đã được cấu hình cho thư viện này! Vui lòng dùng chức năng Cập nhật.");
        }

        FeeConfig feeConfig = FeeConfig.builder()
                .library(library)
                .feeType(request.getFeeType())
                .amount(request.getAmount())
                .build();

        FeeConfig savedConfig = feeConfigRepository.save(feeConfig);
        return mapToResponse(savedConfig);
    }

    // 2. LẤY DANH SÁCH PHÍ CỦA THƯ VIỆN
    public List<FeeConfigResponse> getFeeConfigsByLibrary(Long libraryId) {
        List<FeeConfig> configs = feeConfigRepository.findByLibrary_LibraryId(libraryId);
        return configs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. CẬP NHẬT MỨC PHÍ
    @Transactional
    public FeeConfigResponse updateFeeConfig(Long configId, FeeConfigRequest request) {
        FeeConfig feeConfig = feeConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình phí với ID: " + configId));

        if(!feeConfig.getLibrary().getHasStudentDiscount()){
           if(request.getFeeType().equals(TypeFeeConfig.REG_STUDENT)){
               throw new RuntimeException("Thư viện không áp dụng giảm giá cho sinh viên");
            }
        }

        // Cập nhật lại số tiền (thường chỉ update giá tiền chứ không đổi loại phí)
        feeConfig.setAmount(request.getAmount());

        FeeConfig updatedConfig = feeConfigRepository.save(feeConfig);
        return mapToResponse(updatedConfig);
    }

    // 4. XÓA CẤU HÌNH
    public void deleteFeeConfig(Long configId) {
        if (!feeConfigRepository.existsById(configId)) {
            throw new RuntimeException("Không tìm thấy cấu hình phí với ID: " + configId);
        }
        feeConfigRepository.deleteById(configId);
    }

    // Hàm phụ trợ map Entity -> DTO
    private FeeConfigResponse mapToResponse(FeeConfig feeConfig) {
        return FeeConfigResponse.builder()
                .configId(feeConfig.getConfigId())
                .feeType(feeConfig.getFeeType())
                .amount(feeConfig.getAmount())
                .build();
    }
}
