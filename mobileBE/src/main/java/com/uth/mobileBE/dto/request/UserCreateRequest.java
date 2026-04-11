package com.uth.mobileBE.dto.request;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String fullName;
    private String username;
    private String password;
}
