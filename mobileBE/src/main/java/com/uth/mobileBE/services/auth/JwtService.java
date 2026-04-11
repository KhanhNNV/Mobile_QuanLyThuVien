package com.uth.mobileBE.services.auth;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.uth.mobileBE.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.jwt.secretKey}")
    private String secretKey;

    public String generateAccessToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        Date issueTime = new Date();
        Date expiredTime = Date.from(issueTime.toInstant().plus(30, ChronoUnit.MINUTES));

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issueTime(issueTime)
                .expirationTime(expiredTime)
                // Lưu Role và LibraryId vào Token
                .claim("role", user.getRole().name())
                .claim("libraryId", user.getLibrary() != null ? user.getLibrary().getLibraryId() : null)
                .build();

        return signToken(jwsHeader, claimsSet);
    }

    public String generateRefreshToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        Date issueTime = new Date();
        Date expiredTime = Date.from(issueTime.toInstant().plus(30, ChronoUnit.DAYS));

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issueTime(issueTime)
                .expirationTime(expiredTime)
                .build();

        return signToken(jwsHeader, claimsSet);
    }

    private String signToken(JWSHeader header, JWTClaimsSet claimsSet) {
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(secretKey));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error signing token", e);
        }
    }

    public boolean verifyToken(String token) throws ParseException, JOSEException {
        if (tokenBlacklistService.isBlacklisted(token)) {
            return false;
        }

        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expirationTime.before(new Date())) {
            return false;
        }
        MACVerifier verifier = new MACVerifier(secretKey.getBytes(StandardCharsets.UTF_8));
        return signedJWT.verify(verifier);
    }

    public String extractUsername(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        return signedJWT.getJWTClaimsSet().getSubject();
    }
}
