package com.uth.mobileBE.controllers;
import com.uth.mobileBE.dto.request.FeeConfigRequest;
import com.uth.mobileBE.dto.response.FeeConfigResponse;
import com.uth.mobileBE.services.FeeConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fee-configs")
@RequiredArgsConstructor
public class FeeConfigController {

    private final FeeConfigService feeConfigService;

    // TẠO CẤU HÌNH PHÍ: POST /api/fee-configs
    @PostMapping
    public ResponseEntity<FeeConfigResponse> createFeeConfig(@RequestBody FeeConfigRequest request) {
        FeeConfigResponse response = feeConfigService.createFeeConfig(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // LẤY DS PHÍ THEO THƯ VIỆN: GET /api/fee-configs/library/{libraryId}
    @GetMapping("/library/{libraryId}")
    public ResponseEntity<List<FeeConfigResponse>> getFeeConfigsByLibrary(@PathVariable Long libraryId) {
        List<FeeConfigResponse> responses = feeConfigService.getFeeConfigsByLibrary(libraryId);
        return ResponseEntity.ok(responses);
    }

    // CẬP NHẬT PHÍ: PUT /api/fee-configs/{configId}
    @PutMapping("/{configId}")
    public ResponseEntity<FeeConfigResponse> updateFeeConfig(
            @PathVariable Long configId,
            @RequestBody FeeConfigRequest request) {
        FeeConfigResponse response = feeConfigService.updateFeeConfig(configId, request);
        return ResponseEntity.ok(response);
    }

    // XÓA PHÍ: DELETE /api/fee-configs/{configId}
    @DeleteMapping("/{configId}")
    public ResponseEntity<String> deleteFeeConfig(@PathVariable Long configId) {
        feeConfigService.deleteFeeConfig(configId);
        return ResponseEntity.ok("Xóa cấu hình phí thành công!");
    }
}
