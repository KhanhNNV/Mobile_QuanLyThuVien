package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.UserRequest;
import com.uth.mobileBE.dto.response.UserResponse;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.User;
import com.uth.mobileBE.repositories.LibraryRepository;
import com.uth.mobileBE.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder; // Un-comment khi có Spring Security
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryRepository libraryRepository;

     @Autowired
     private PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserRequest request) {
        // Kiểm tra Library có tồn tại không
        Library library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Library với id: " + request.getLibraryId()));

        // Kiểm tra Username đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại: " + request.getUsername());
        }

        LocalDateTime now = LocalDateTime.now();

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .library(library)
                .username(request.getUsername())
                .passwordHash(encodedPassword)
                .fullname(request.getFullname())
                .role(request.getRole())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdAt(now)
                .updateAt(now)
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = getUserEntityById(id);
        return mapToResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User existingUser = getUserEntityById(id);

        if (request.getFullname() != null) {
            existingUser.setFullname(request.getFullname());
        }
        if (request.getRole() != null) {
            existingUser.setRole(request.getRole());
        }
        if (request.getIsActive() != null) {
            existingUser.setIsActive(request.getIsActive());
        }

        // Cập nhật mật khẩu nếu có gửi lên
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // Cập nhật Library nếu có đổi
        if (request.getLibraryId() != null) {
            Library library = libraryRepository.findById(request.getLibraryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Library"));
            existingUser.setLibrary(library);
        }

        // Lưu ý: Thường không cho phép đổi username, hoặc nếu đổi phải check unique
        if (request.getUsername() != null && !request.getUsername().equals(existingUser.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username đã tồn tại!");
            }
            existingUser.setUsername(request.getUsername());
        }

        existingUser.setUpdateAt(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);
        return mapToResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = getUserEntityById(id);
        userRepository.delete(user);
    }

    // --- CÁC HÀM HỖ TRỢ NỘI BỘ ---

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User với id: " + id));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .libraryId(user.getLibrary().getLibraryId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updateAt(user.getUpdateAt())
                .build();
    }
}
