package com.osy.sharetodo.feature.auth.service;

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

    public void store(String sub, String jti, String rawToken, String famId,
                      Duration ttl, String uaHash, String ip) {
        String hashHex = sha256Hex(rawToken);

        String k = RT + sub + ":" + jti;
        redis.opsForHash().putAll(k, Map.of(
                "hash", hashHex,
                "fam", famId,
                "ua", uaHash == null ? "" : uaHash,
                "ip", ip == null ? "" : ip
        ));
        redis.expire(k, ttl);

        redis.opsForValue().set(RTH + hashHex, sub + ":" + jti, ttl);
    }

    public Optional<Entry> resolveByRawToken(String rawToken) {
        String hashHex = sha256Hex(rawToken);
        String ref = redis.opsForValue().get(RTH + hashHex);
        if (ref == null) return Optional.empty();
        String[] parts = ref.split(":", 2);
        if (parts.length != 2) return Optional.empty();
        String sub = parts[0], jti = parts[1];

        String fam = (String) redis.opsForHash().get(RT + sub + ":" + jti, "fam");
        return Optional.of(new Entry(sub, jti, fam, hashHex));
    }

    public void revoke(String sub, String jti, String hashHex) {
        redis.delete(RT + sub + ":" + jti);
        redis.delete(RTH + hashHex);
    }

    public void revokeFamily(String sub, String famId, Duration ttl) {
        String key = FAM_REVOKED + sub + ":" + famId + ":revoked";
        redis.opsForValue().set(key, "1", ttl);
    }

    public boolean isFamilyRevoked(String sub, String famId) {
        String key = FAM_REVOKED + sub + ":" + famId + ":revoked";
        return redis.opsForValue().get(key) != null;
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