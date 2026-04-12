package com.uth.mobileBE.controllers;

import com.uth.mobileBE.dto.request.InitialBookRequest;
import com.uth.mobileBE.dto.response.CategoryResponse;
import com.uth.mobileBE.dto.response.InitialBookResponse;
import com.uth.mobileBE.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @PostMapping("/welcome")
    public ResponseEntity<InitialBookResponse> createInitialBook(@RequestBody InitialBookRequest request){
        InitialBookResponse response = bookService.createInitialBook(request);
        return ResponseEntity.ok(response);
    }
}
