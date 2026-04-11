package com.uth.mobileBE.services.auth;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public void addToBlacklist(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
            long remainingTime = expirationDate.getTime() - System.currentTimeMillis();

            if (remainingTime > 0) {
                // Lưu token vào Redis, tự động xóa khi token thực sự hết hạn
                redisTemplate.opsForValue().set("BL:" + token, "revoked", remainingTime, TimeUnit.MILLISECONDS);
            }
        } catch (ParseException e) {
            // Bỏ qua nếu token sai định dạng
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("BL:" + token));
    }
}
