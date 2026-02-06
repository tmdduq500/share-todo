package com.osy.sharetodo.feature.account.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailVerificationCodeService {

    private static final SecureRandom RND = new SecureRandom();
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(60);
    private final StringRedisTemplate redis;

    private static String normEmail(String email) {
        return StringUtils.trimToEmpty(email).toLowerCase(Locale.ROOT);
    }

    private static String kCode(String emailNorm) {
        return "email:verify:code:" + emailNorm;
    }

    private static String kOk(String emailNorm) {
        return "email:verify:ok:" + emailNorm;
    }

    private static String kCooldown(String emailNorm) {
        return "email:verify:cooldown:" + emailNorm;
    }

    public String issueCode(String emailRaw) {
        String emailNorm = normEmail(emailRaw);

        // 60초 쿨다운
        String cooldown = redis.opsForValue().get(kCooldown(emailNorm));
        if (cooldown != null) {
            return null;
        }

        String code = String.format("%06d", RND.nextInt(1_000_000));
        redis.opsForValue().set(kCode(emailNorm), code, CODE_TTL);
        redis.opsForValue().set(kCooldown(emailNorm), "1", COOLDOWN_TTL);

        // 이전에 verified 찍혀있던 기록은 새 코드 발급 시 무효화
        redis.delete(kOk(emailNorm));

        return code;
    }

    public boolean confirm(String emailRaw, String code) {
        String emailNorm = normEmail(emailRaw);
        String stored = redis.opsForValue().get(kCode(emailNorm));
        if (stored == null) return false;
        if (!stored.equals(code)) return false;

        // 성공 처리: ok 플래그 저장 + code 삭제
        redis.opsForValue().set(kOk(emailNorm), "1", VERIFIED_TTL);
        redis.delete(kCode(emailNorm));
        return true;
    }

    public boolean isVerified(String emailRaw) {
        String emailNorm = normEmail(emailRaw);
        return redis.opsForValue().get(kOk(emailNorm)) != null;
    }
}
