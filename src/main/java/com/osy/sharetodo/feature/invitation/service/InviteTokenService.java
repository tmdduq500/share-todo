package com.osy.sharetodo.feature.invitation.service;

import com.osy.sharetodo.global.util.Hashing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class InviteTokenService {
    private final Hashing hashing;
    private static final SecureRandom RNG = new SecureRandom();

    /** 32바이트 랜덤 → Base64url(패딩X) 약 43자 */
    public String newToken() {
        byte[] buf = new byte[32];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /** 토큰 원문 → SHA-256(32바이트) */
    public byte[] hash(String rawToken) {
        return hashing.sha256(rawToken);
    }
}