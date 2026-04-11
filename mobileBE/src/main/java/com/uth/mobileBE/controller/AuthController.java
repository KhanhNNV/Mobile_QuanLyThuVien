package com.uth.mobileBE.controller;

import com.uth.mobileBE.dto.request.LoginRequest;
import com.uth.mobileBE.dto.request.LogoutRequest;
import com.uth.mobileBE.dto.request.RefreshTokenRequest;
import com.uth.mobileBE.dto.request.RegisterRequest;
import com.uth.mobileBE.dto.response.LoginResponse;
import com.uth.mobileBE.services.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authenticationService.register(request);
        return ResponseEntity.ok("Đăng ký tài khoản thành công!");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        LoginResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody LogoutRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        authenticationService.logout(request, authHeader);
        return ResponseEntity.ok("Đăng xuất thành công!");
    }


}