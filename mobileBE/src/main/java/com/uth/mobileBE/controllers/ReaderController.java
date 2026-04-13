package com.uth.mobileBE.controllers;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.ReaderRequest;
import com.uth.mobileBE.dto.response.ReaderResponse;
import com.uth.mobileBE.services.ReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readers")
@RequiredArgsConstructor
public class ReaderController {
    private final ReaderService readerService;

    @PostMapping
    public ResponseEntity<ReaderResponse> create(@RequestBody ReaderRequest request) {
        return ResponseEntity.ok(readerService.createReader(request));
    }

    @GetMapping
    public ResponseEntity<List<ReaderResponse>> getAll() {
        return ResponseEntity.ok(readerService.getAllReaders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReaderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(readerService.getReaderById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReaderResponse> update(@PathVariable Long id, @RequestBody ReaderRequest request) {
        return ResponseEntity.ok(readerService.updateReader(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        readerService.deleteReader(id);
        return ResponseEntity.ok("Xóa độc giả thành công");
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countReaders() {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(readerService.countReaders(libraryId));
    }
}