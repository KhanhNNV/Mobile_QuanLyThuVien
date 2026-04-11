package com.uth.mobileBE.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RegisterRequest {
    String username;
    String password;
    String fullName;
    Long libraryId;
}