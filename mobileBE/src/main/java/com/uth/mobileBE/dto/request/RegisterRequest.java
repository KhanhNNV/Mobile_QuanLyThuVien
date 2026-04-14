package com.uth.mobileBE.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String libraryName;
    private String address;
}