package com.uth.mobileBE.dto.request;


import com.uth.mobileBE.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String username;
    private String fullname;
    private Role role;
    private Boolean isActive;
    private String password;
}
