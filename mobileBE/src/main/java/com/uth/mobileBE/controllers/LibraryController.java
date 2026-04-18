package com.uth.mobileBE.controllers;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.LibraryRequest;
import com.uth.mobileBE.dto.response.LibraryResponse;
import com.uth.mobileBE.services.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/libraries")
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    @PostMapping
    public ResponseEntity<LibraryResponse> createLibrary(@RequestBody LibraryRequest request) {
        LibraryResponse response = libraryService.createLibrary(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }



    @GetMapping()
    public ResponseEntity<LibraryResponse> getLibraryById() {
        return ResponseEntity.ok(libraryService.getLibraryById());
    }

    @PutMapping()
    public ResponseEntity<LibraryResponse> updateLibrary(
            @RequestBody LibraryRequest request) {
        return ResponseEntity.ok(libraryService.updateLibrary(request));
    }
    @PutMapping("/loansQuota")
    public ResponseEntity<LibraryResponse> updateLibraryLoansQuota(
            @RequestParam Integer maxLoansQuota) {
        return ResponseEntity.ok(libraryService.updateMaxLoansQuota(maxLoansQuota));
    }
    @PutMapping("/booksQuota")
    public ResponseEntity<LibraryResponse> updateLibraryBooksQuota(
            @RequestParam Integer maxBookssQuota) {
        return ResponseEntity.ok(libraryService.updateMaxBooksQuota(maxBookssQuota));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLibrary(@PathVariable Long id) {
        libraryService.deleteLibrary(id);
        return ResponseEntity.noContent().build();
    }
}