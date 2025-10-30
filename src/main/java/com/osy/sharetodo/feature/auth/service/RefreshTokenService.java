package com.osy.sharetodo.feature.auth.service;

import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redis;

    private static final String RT = "auth:rt:";         // auth:rt:<sub>:<jti>
    private static final String RTH = "auth:rthash:";    // auth:rthash:<hashHex> -> "<sub>:<jti>"
    private static final String FAM_REVOKED = "auth:rtfam:"; // auth:rtfam:<sub>:<fam>:revoked

    public void store(String sub, String jti, String rawToken, String famId, Duration ttl) {
        try {
            String hashHex = sha256Hex(rawToken);
            String k = RT + sub + ":" + jti;
            redis.opsForHash().putAll(k, Map.of("hash", hashHex, "fam", famId));
            redis.expire(k, ttl);
            redis.opsForValue().set(RTH + hashHex, sub + ":" + jti, ttl);
        } catch (RuntimeException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "토큰 저장 중 오류가 발생했습니다.");
        }
    }


    public Optional<Entry> resolveByRawToken(String rawToken) {
        try {
            String hashHex = sha256Hex(rawToken);
            String ref = redis.opsForValue().get(RTH + hashHex);
            if (ref == null) return Optional.empty();
            String[] parts = ref.split(":", 2);
            if (parts.length != 2) return Optional.empty();
            String sub = parts[0], jti = parts[1];
            String fam = (String) redis.opsForHash().get(RT + sub + ":" + jti, "fam");
            return Optional.of(new Entry(sub, jti, fam, hashHex));
        } catch (RuntimeException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "토큰 조회 중 오류가 발생했습니다.");
        }
    }


    public void revoke(String sub, String jti, String hashHex) {
        try {
            redis.delete(RT + sub + ":" + jti);
            redis.delete(RTH + hashHex);
        } catch (RuntimeException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "토큰 삭제 중 오류가 발생했습니다.");
        }
    }


    public void revokeFamily(String sub, String famId, Duration ttl) {
        try {
            String key = FAM_REVOKED + sub + ":" + famId + ":revoked";
            redis.opsForValue().set(key, "1", ttl);
        } catch (RuntimeException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "패밀리 토큰 폐기 중 오류가 발생했습니다.");
        }
    }


    public boolean isFamilyRevoked(String sub, String famId) {
        try {
            String key = FAM_REVOKED + sub + ":" + famId + ":revoked";
            return redis.opsForValue().get(key) != null;
        } catch (RuntimeException e) {
            return false; // fallback: 비정상인 경우 허용 처리
        }
    }

    public static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Entry {
        public final String sub;
        public final String jti;
        public final String fam;
        public final String hashHex;
        public Entry(String sub, String jti, String fam, String hashHex) {
            this.sub = sub; this.jti = jti; this.fam = fam; this.hashHex = hashHex;
        }
    }
}