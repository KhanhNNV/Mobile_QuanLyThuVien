package com.uth.mobileBE.services;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.UserRequest;
import com.uth.mobileBE.dto.response.UserResponse;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.User;
import com.uth.mobileBE.models.enums.Role;
import com.uth.mobileBE.repositories.LibraryRepository;
import com.uth.mobileBE.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder; // Un-comment khi có Spring Security
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsersByCurrentLibrary() {
        Long libraryId = SecurityUtils.getLibraryId();
        List<User> users = userRepository.findByLibrary_LibraryId(libraryId);
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        // Kiểm tra user có thuộc cùng library không
        validateSameLibrary(user);

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserRequest request) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        // Kiểm tra user có thuộc cùng library không
        validateSameLibrary(targetUser);

        // Lấy thông tin user hiện tại đang thực hiện request
        String username = SecurityUtils.getUsername();
        User currentUser = userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("Không tìm thấy user này"));

        // Kiểm tra các quy tắc khi update role
        validateRoleUpdate(currentUser, targetUser, request.getRole());

        // Cập nhật thông tin
        if (request.getFullname() != null && !request.getFullname().trim().isEmpty()) {
            targetUser.setFullname(request.getFullname());
        }

        if (request.getRole() != null) {
            targetUser.setRole(request.getRole());
        }
        if(request.getPassword() !=null && !request.getPassword().trim().isEmpty()){
            targetUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getIsActive() != null) {
            // Không cho phép tự vô hiệu hóa chính mình
            if (currentUser.getUserId().equals(userId) && !request.getIsActive()) {
                throw new IllegalArgumentException("Không thể tự vô hiệu hóa tài khoản của chính mình");
            }
            targetUser.setIsActive(request.getIsActive());
        }

        User updatedUser = userRepository.save(targetUser);
        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        // Kiểm tra user có thuộc cùng library không
        validateSameLibrary(targetUser);

        // Lấy thông tin user hiện tại
        String username = SecurityUtils.getUsername();
        User currentUser = userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("Không tìm thấy user này"));

        // Không cho phép tự xóa chính mình
        if (currentUser.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Không thể xóa tài khoản của chính mình");
        }

        // Kiểm tra nếu user đang bị xóa là admin cuối cùng
        if (targetUser.getRole() == Role.ADMIN) {
            long activeAdminCount = userRepository.countActiveAdminsByLibrary(
                    targetUser.getLibrary(),
                    Role.ADMIN
            );

            if (activeAdminCount <= 1) {
                throw new IllegalStateException(
                        "Không thể xóa admin cuối cùng của thư viện. " +
                                "Thư viện phải có ít nhất một admin đang hoạt động."
                );
            }
        }

        userRepository.delete(targetUser);
    }

    private void validateRoleUpdate(User currentUser, User targetUser, Role newRole) {
        // Nếu không thay đổi role thì không cần kiểm tra
        if (newRole == null || targetUser.getRole() == newRole) {
            return;
        }

        // Trường hợp đổi từ ADMIN sang STAFF
        if (targetUser.getRole() == Role.ADMIN && newRole == Role.STAFF) {
            // Không cho phép admin tự đổi role của chính mình
            if (currentUser.getUserId().equals(targetUser.getUserId())) {
                throw new AccessDeniedException("Không thể tự thay đổi role của chính mình");
            }

            // Kiểm tra đảm bảo còn ít nhất 1 admin active khác
            long activeAdminCount = userRepository.countActiveAdminsByLibrary(
                    targetUser.getLibrary(),
                    Role.ADMIN
            );

            if (activeAdminCount <= 1) {
                throw new IllegalStateException(
                        "Không thể thay đổi role của admin cuối cùng. " +
                                "Thư viện phải có ít nhất một admin đang hoạt động."
                );
            }
        }
    }

    private void validateSameLibrary(User user) {
        Long currentLibraryId = SecurityUtils.getLibraryId();
        if (!user.getLibrary().getLibraryId().equals(currentLibraryId)) {
            throw new AccessDeniedException("Không có quyền truy cập user từ thư viện khác");
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updateAt(user.getUpdateAt())
                .build();
    }
}
