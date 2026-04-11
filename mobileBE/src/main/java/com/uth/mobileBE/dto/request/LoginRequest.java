package com.uth.mobileBE.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LoginRequest {
    private String username;
    private String password;
}
