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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        BookResponse response = bookCrudService.createBook(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/library/{libraryId}")
    public ResponseEntity<List<BookResponse>> getBooksByLibrary(@PathVariable Long libraryId) {
        return ResponseEntity.ok(bookCrudService.getBooksByLibrary(libraryId));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long bookId) {
        return ResponseEntity.ok(bookCrudService.getBookById(bookId));
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long bookId,
            @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookCrudService.updateBook(bookId, request));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable Long bookId) {
        bookCrudService.deleteBook(bookId);
        return ResponseEntity.ok("Xóa sách thành công!");
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
