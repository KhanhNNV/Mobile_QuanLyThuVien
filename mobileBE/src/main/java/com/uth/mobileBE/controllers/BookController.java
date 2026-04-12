package com.uth.mobileBE.controllers;

import com.uth.mobileBE.dto.request.BookRequest;
import com.uth.mobileBE.dto.request.InitialBookRequest;
import com.uth.mobileBE.dto.response.BookResponse;
import com.uth.mobileBE.dto.response.InitialBookResponse;
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

    @PostMapping("/welcome")
    public ResponseEntity<InitialBookResponse> createInitialBook(@RequestBody InitialBookRequest request) {
        InitialBookResponse response = bookService.createInitialBook(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@RequestBody BookRequest request) {
        BookResponse created = bookService.createBook(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/library/{libraryId}")
    public ResponseEntity<List<BookResponse>> getBooksByLibrary(@PathVariable Long libraryId) {
        return ResponseEntity.ok(bookService.getAllBooksByLibrary(libraryId));
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long bookId,
            @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(bookId, request));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable Long bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.ok("Xóa sách thành công!");
    }
}
