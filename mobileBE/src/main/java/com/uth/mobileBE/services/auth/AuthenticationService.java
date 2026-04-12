package com.uth.mobileBE.services.auth;

import com.nimbusds.jose.JOSEException;
import com.uth.mobileBE.dto.request.LoginRequest;
import com.uth.mobileBE.dto.request.LogoutRequest;
import com.uth.mobileBE.dto.request.RefreshTokenRequest;
import com.uth.mobileBE.dto.request.RegisterRequest;
import com.uth.mobileBE.dto.response.LoginResponse;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.User;
import com.uth.mobileBE.models.enums.Role;
import com.uth.mobileBE.models.enums.StatusLibrary;
import com.uth.mobileBE.repositories.LibraryRepository;
import com.uth.mobileBE.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LibraryRepository libraryRepository;
    private final TokenBlacklistService tokenBlacklistService;

    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return LoginResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã được sử dụng!");
        }

        Library newLibrary = Library.builder()
                .name(request.getLibraryName())
                .address(request.getAddress())
                .hasStudentDiscount(request.getHasStudentDiscount() != null && request.getHasStudentDiscount())
                .status(StatusLibrary.ACTIVE)
                .createdAt(LocalDateTime.now())
                // Tặng 30 ngày (tính bằng milliseconds) dùng thử phí nền tảng
                .platformFeeExpiry(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))
                .build();

        Library savedLibrary = libraryRepository.save(newLibrary);

        User newUser = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullname(request.getFullName())
                .library(savedLibrary)
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        userRepository.save(newUser);
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        try {
            String token = request.getRefreshToken();

            if (!jwtService.verifyToken(token)) {
                throw new RuntimeException("Refresh Token không hợp lệ hoặc đã hết hạn!");
            }

            String username = jwtService.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            if (!user.getIsActive()) {
                throw new RuntimeException("Tài khoản đã bị khóa!");
            }

            tokenBlacklistService.addToBlacklist(token);

            return LoginResponse.builder()
                    .accessToken(jwtService.generateAccessToken(user))
                    .refreshToken(jwtService.generateRefreshToken(user))
                    .build();

        } catch (ParseException | JOSEException e) {
            throw new RuntimeException("Lỗi xử lý Refresh Token", e);
        }
    }

    public void logout(LogoutRequest request, String authHeader) {
        try {
            // 1. Đưa Refresh Token vào Blacklist
            String refreshToken = request.getRefreshToken();
            if (refreshToken != null && !refreshToken.isEmpty()) {
                // Không cần gọi verifyToken vì có thể token đã hết hạn, cứ tống thẳng vào Redis
                tokenBlacklistService.addToBlacklist(refreshToken);
            }

            // 2. Đưa Access Token vào Blacklist
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7); // Cắt chữ "Bearer "
                tokenBlacklistService.addToBlacklist(accessToken);
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý khi đăng xuất", e);
        }
    }

}
