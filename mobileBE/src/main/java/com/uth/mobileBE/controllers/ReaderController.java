package com.uth.mobileBE.controllers;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.ExtendMembershipExpiryRequest;
import com.uth.mobileBE.dto.request.ReaderRequest;
import com.uth.mobileBE.dto.response.ReaderResponse;
import com.uth.mobileBE.services.ReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readers")
@RequiredArgsConstructor
public class ReaderController {
    private final ReaderService readerService;

    /**
     * Tạo mới một độc giả
     * @param request thông tin độc giả cần tạo
     * @return thông tin độc giả đã tạo
     */
    @PostMapping
    public ResponseEntity<ReaderResponse> create(@RequestBody ReaderRequest request) {
        return ResponseEntity.ok(readerService.createReader(request));
    }

    /**
     * Lấy danh sách tất cả độc giả
     * @return danh sách độc giả
     */
    @GetMapping("/all")
    public ResponseEntity<List<ReaderResponse>> getAll() {
        return ResponseEntity.ok(readerService.getAllReaders());
    }

    /**
     * Lấy thông tin một độc giả theo ID
     * @param id mã độc giả
     * @return thông tin độc giả
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReaderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(readerService.getReaderById(id));
    }

    /**
     * Cập nhật thông tin độc giả theo ID
     * @param id mã độc giả
     * @param request thông tin cập nhật
     * @return thông tin độc giả sau khi cập nhật
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReaderResponse> update(@PathVariable Long id, @RequestBody ReaderRequest request) {
        return ResponseEntity.ok(readerService.updateReader(id, request));
    }

    /**
     * Xóa độc giả theo ID
     * @param id mã độc giả
     * @return thông báo xóa thành công
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        readerService.deleteReader(id);
        return ResponseEntity.ok("Xóa độc giả thành công");
    }


    /**
     * Đếm số lượng độc giả theo thư viện
     * @param `libraryId` mã thư viện
     * @return số lượng độc giả
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countReaders() {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(readerService.countReaders(libraryId));
    }

    /**
     * Search độc giả
     * @param `fullName`, `phone`, `barcode`
     * @return List reader
     */
    @GetMapping("/search")
    public ResponseEntity<List<ReaderResponse>> searchReaders(@RequestParam String query) {
        return ResponseEntity.ok(readerService.searchListReader(query));
    }


    /**
     * Lấy danh sách độc giả+ Phân trang
     * @parem `page`, 'size`
     */
    @GetMapping
    public ResponseEntity<?> getReadersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        return ResponseEntity.ok(readerService.getReadersPaginated(page,size));
    }

    @PutMapping("/{id}/extend")
    public ResponseEntity<ReaderResponse> extendMembershipExpiry(@PathVariable Long id,@RequestBody ExtendMembershipExpiryRequest request) {
        return ResponseEntity.ok(readerService.extendMembershipExpiry(id, request));
    }

}