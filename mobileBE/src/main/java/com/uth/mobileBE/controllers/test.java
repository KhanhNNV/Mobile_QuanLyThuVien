package com.uth.mobileBE.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class test {
    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello World");
    }
}
