package com.uth.mobileBE.Utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtils {

    public static Jwt getJwt() {
        return (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public static Long getLibraryId() {
        return getJwt().getClaim("libraryId");
    }

    public static String getUsername() {
        return getJwt().getSubject();
    }
}
