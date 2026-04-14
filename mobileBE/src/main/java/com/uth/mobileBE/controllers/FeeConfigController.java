package com.uth.mobileBE.controllers;
import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.FeeConfigRequest;
import com.uth.mobileBE.dto.response.FeeConfigResponse;
import com.uth.mobileBE.services.FeeConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fee-configs")
@RequiredArgsConstructor
public class FeeConfigController {

    private final FeeConfigService feeConfigService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FeeConfigResponse> createOrUpdateFeeConfig(@RequestBody FeeConfigRequest request) {
        Long libraryId = SecurityUtils.getLibraryId();
        FeeConfigResponse response = feeConfigService.createOrUpdateFeeConfig(request, libraryId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // LẤY DS PHÍ THEO THƯ VIỆN: GET /api/fee-configs
    @GetMapping()
    public ResponseEntity<List<FeeConfigResponse>> getFeeConfigsByLibrary() {
        Long libraryId= SecurityUtils.getLibraryId();
        List<FeeConfigResponse> responses = feeConfigService.getFeeConfigsByLibrary(libraryId);
        return ResponseEntity.ok(responses);
    }

    // XÓA PHÍ: DELETE /api/fee-configs/{configId}
    @DeleteMapping("/{configId}")
    public ResponseEntity<String> deleteFeeConfig(@PathVariable Long configId) {
        feeConfigService.deleteFeeConfig(configId);
        return ResponseEntity.ok("Xóa cấu hình phí thành công!");
    }
}
