package com.osy.sharetodo.feature.auth.service;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.auth.dto.AuthDto;
import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import com.osy.sharetodo.global.security.JwtProps;
import com.osy.sharetodo.global.security.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProps jwtProps;
    private final RefreshTokenService refreshTokenService;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public AuthDto.Tokens login(AuthDto.LoginReq req) {
        Account acc = accountRepository.findByEmailNorm(req.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Invalid credentials"));

        String stored = acc.getPasswordHash() == null ? "" :
                new String(acc.getPasswordHash(), StandardCharsets.UTF_8);
        if (!passwordEncoder.matches(req.getPassword(), stored)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid credentials");
        }

        String at = jwtProvider.createAccessToken(acc.getUid());
        String fam = UUID.randomUUID().toString();
        String jti = UUID.randomUUID().toString();
        String rt = jti;

        refreshTokenService.store(
                acc.getUid(),
                jti,
                rt,
                fam,
                Duration.ofDays(jwtProps.getRefreshTokenTtlDays())
        );

        return new AuthDto.Tokens(at, rt);
    }

    @Transactional
    public AuthDto.Tokens refresh(AuthDto.RefreshReq req) {
        var found = refreshTokenService.resolveByRawToken(req.getRefreshToken())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshTokenService.isFamilyRevoked(found.sub, found.fam)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Session revoked");
        }

        refreshTokenService.revoke(found.sub, found.jti, found.hashHex);

        String newAt = jwtProvider.createAccessToken(found.sub);
        String newJti = UUID.randomUUID().toString();
        String newRt = newJti;

        refreshTokenService.store(
                found.sub,
                newJti,
                newRt,
                found.fam,
                Duration.ofDays(jwtProps.getRefreshTokenTtlDays())
        );

        return new AuthDto.Tokens(newAt, newRt);
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        String jti = jwtProvider.parse(accessToken).getBody().getId();
        long ttl = jwtProvider.parse(accessToken).getBody().getExpiration().getTime() - System.currentTimeMillis();


        try {
            redisTemplate.opsForValue().set("blacklist:access:" + jti, "true", Duration.ofMillis(ttl));
        } catch (RuntimeException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "로그아웃 처리 중 Redis 오류가 발생했습니다.");
        }


        RefreshTokenService.Entry resolved = refreshTokenService.resolveByRawToken(refreshToken)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."));


        refreshTokenService.revoke(resolved.sub, resolved.jti, resolved.hashHex);
    }
}