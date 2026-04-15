package com.uth.mobileBE.controllers;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.BookRequest;
import com.uth.mobileBE.dto.request.InitialBookRequest;
import com.uth.mobileBE.dto.response.BookResponse;
import com.uth.mobileBE.dto.response.InitialBookResponse;
import com.uth.mobileBE.services.BookCrudService;
import com.uth.mobileBE.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    private final BookCrudService bookCrudService;

    @PostMapping("/welcome")
    public ResponseEntity<InitialBookResponse> createInitialBook(@RequestBody InitialBookRequest request) {
        InitialBookResponse response = bookService.createInitialBook(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@RequestBody BookRequest request) {
        Long currentLibraryId = SecurityUtils.getLibraryId();
        BookResponse response = bookCrudService.createBook(currentLibraryId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getBooksByCurrentLibrary() {
        Long currentLibraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(bookCrudService.getBooksByLibrary(currentLibraryId));
    }

    @GetMapping("/library/{libraryId}")
    public ResponseEntity<List<BookResponse>> getBooksByLibrary(@PathVariable Long libraryId) {
        return ResponseEntity.ok(bookCrudService.getBooksByLibrary(libraryId));
    }

    @GetMapping("/id/{bookId}")
    public ResponseEntity<BookResponse> getBookByIdWithIdPrefix(@PathVariable Long bookId) {
        return ResponseEntity.ok(bookCrudService.getBookById(bookId));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long bookId) {
        return ResponseEntity.ok(bookCrudService.getBookById(bookId));
    }

    @PutMapping("/id/{bookId}")
    public ResponseEntity<BookResponse> updateBookWithIdPrefix(
            @PathVariable Long bookId,
            @RequestBody BookRequest request) {
        Long currentLibraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(bookCrudService.updateBook(currentLibraryId, bookId, request));
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long bookId,
            @RequestBody BookRequest request) {
        Long currentLibraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(bookCrudService.updateBook(currentLibraryId, bookId, request));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable Long bookId) {
        bookCrudService.deleteBook(bookId);
        return ResponseEntity.ok("Xóa sách thành công!");
    }


    @GetMapping("/current-library-id")
    public ResponseEntity<Long> getCurrentLibraryId() {
        return ResponseEntity.ok(SecurityUtils.getLibraryId());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countBooksByLibrary() {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(bookCrudService.countAllBookByLibrary(libraryId));
    }

    //lấy số sách có copy available <2
    @GetMapping("/alerts/low-copies")
    public ResponseEntity<List<String>> getLowCopyAlerts() {
        Long libraryId = SecurityUtils.getLibraryId();
        return ResponseEntity.ok(bookService.getLowCopyAlerts(libraryId));
    }
}
