package com.uth.mobileBE.services.auth;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
// Bật class này khi app.redis.enabled = false
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "false")
public class InMemoryTokenBlacklistServiceImpl implements TokenBlacklistService {

    // Dùng Map để lưu token và thời gian hết hạn (Timestamp)
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    @Override
    public void addToBlacklist(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();

            // Lưu token cùng thời điểm nó sẽ hết hạn
            blacklist.put(token, expirationDate.getTime());
        } catch (ParseException e) {
            // Bỏ qua nếu token sai định dạng
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        Long expirationTime = blacklist.get(token);

        if (expirationTime == null) {
            return false;
        }

        // Nếu token đã quá hạn so với thời gian hiện tại thì tự động xóa khỏi Map và trả về false
        if (System.currentTimeMillis() > expirationTime) {
            blacklist.remove(token);
            return false;
        }

        return true; // Token có trong blacklist và chưa hết hạn
    }
}