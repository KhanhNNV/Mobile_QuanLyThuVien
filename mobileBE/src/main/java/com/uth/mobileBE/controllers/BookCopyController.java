package com.uth.mobileBE.controllers;

import com.uth.mobileBE.dto.request.BookCopyRequest;
import com.uth.mobileBE.dto.response.BookCopyResponse;
import com.uth.mobileBE.services.BookCopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-copies")
@RequiredArgsConstructor
public class BookCopyController {

    private final BookCopyService bookCopyService;

    @PostMapping
    public ResponseEntity<BookCopyResponse> createBookCopy(@RequestBody BookCopyRequest request) {
        BookCopyResponse created = bookCopyService.createBookCopy(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<BookCopyResponse>> getCopiesByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(bookCopyService.getAllCopiesByBook(bookId));
    }

    @PutMapping("/{copyId}")
    public ResponseEntity<BookCopyResponse> updateBookCopy(
            @PathVariable Long copyId,
            @RequestBody BookCopyRequest request) {
        return ResponseEntity.ok(bookCopyService.updateBookCopy(copyId, request));
    }

    @DeleteMapping("/{copyId}")
    public ResponseEntity<String> deleteBookCopy(@PathVariable Long copyId) {
        bookCopyService.deleteBookCopy(copyId);
        return ResponseEntity.ok("Xóa bản sao thành công!");
    }
}
