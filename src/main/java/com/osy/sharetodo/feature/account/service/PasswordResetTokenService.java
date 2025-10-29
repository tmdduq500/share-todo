package com.osy.sharetodo.feature.account.service;

import com.osy.sharetodo.global.util.Hashing;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private final StringRedisTemplate redis;
    private final Hashing hashing;

    private static final SecureRandom RNG = new SecureRandom();
    private static final String PRH = "auth:prhash:"; // auth:prhash:<hashHex> -> email
    private static final String PRRL = "auth:prrate:"; // rate limit per email (optional)

    public String newToken() {
        byte[] buf = new byte[32];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf); // ~43자
    }

    public String hashHex(String raw) {
        byte[] h = hashing.sha256(raw);
        StringBuilder sb = new StringBuilder(h.length * 2);
        for (byte b : h) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /** 토큰 발급: 토큰 원문 반환, Redis엔 해시->이메일 매핑 저장 */
    public String issue(String emailNorm, Duration ttl) {
        String token = newToken();
        String hex = hashHex(token);
        redis.opsForValue().set(PRH + hex, emailNorm, ttl);
        return token;
    }

    /** 토큰 해시로 이메일 복원 후 1회성 사용을 위해 삭제 */
    public String consume(String tokenRaw) {
        String hex = hashHex(tokenRaw);
        String email = redis.opsForValue().get(PRH + hex);
        if (email != null) {
            redis.delete(PRH + hex); // 1회성
        }
        return email; // null이면 무효/만료
    }
}