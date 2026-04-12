package com.uth.mobileBE.controllers;

import com.uth.mobileBE.dto.request.LibraryRequest;
import com.uth.mobileBE.dto.response.LibraryResponse;
import com.uth.mobileBE.services.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<LibraryResponse>> getAllLibraries() {
        return ResponseEntity.ok(libraryService.getAllLibraries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibraryResponse> getLibraryById(@PathVariable Long id) {
        return ResponseEntity.ok(libraryService.getLibraryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LibraryResponse> updateLibrary(
            @PathVariable Long id,
            @RequestBody LibraryRequest request) {
        return ResponseEntity.ok(libraryService.updateLibrary(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLibrary(@PathVariable Long id) {
        libraryService.deleteLibrary(id);
        return ResponseEntity.noContent().build();
    }
}