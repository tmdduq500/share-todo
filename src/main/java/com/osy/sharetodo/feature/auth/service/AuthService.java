package com.osy.sharetodo.feature.auth.service;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.auth.dto.AuthDto;
import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import com.osy.sharetodo.global.security.JwtProps;
import com.osy.sharetodo.global.security.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProps jwtProps;
    private final RefreshTokenService rtService;

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

        // RT = UUID(opaque). jti도 UUID로 동일하게 사용
        String fam = UUID.randomUUID().toString();
        String jti = UUID.randomUUID().toString();
        String rt = jti; // 토큰 값 자체로 UUID 사용

        rtService.store(acc.getUid(), jti, rt, fam,
                Duration.ofDays(jwtProps.getRefreshTokenTtlDays()),
                "ua", "ip"); // TODO: 실제 UA/IP 입력

        return new AuthDto.Tokens(at, rt);
    }

    @Transactional
    public AuthDto.Tokens refresh(AuthDto.RefreshReq req) {
        // 1) 해시 인덱스로 역조회
        Optional<RefreshTokenService.Entry> found = rtService.resolveByRawToken(req.getRefreshToken());
        if (found.isEmpty()) {
            // 존재하지 않음 = 만료/회전된 토큰 재사용 가능성 → (옵션) fam revoke
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid refresh token");
        }
        RefreshTokenService.Entry e = found.get();

        // (옵션) fam 전체 차단이 활성화된 경우
        if (rtService.isFamilyRevoked(e.sub, e.fam)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Session revoked");
        }

        // 2) 정상 회전: 이전 키 폐기 → 새 RT 발급/저장
        rtService.revoke(e.sub, e.jti, e.hashHex);

        String newAt = jwtProvider.createAccessToken(e.sub);
        String newJti = UUID.randomUUID().toString();
        String newRt = newJti;

        rtService.store(e.sub, newJti, newRt, e.fam,
                Duration.ofDays(jwtProps.getRefreshTokenTtlDays()),
                req.getUserAgentHash(), req.getIp());

        return new AuthDto.Tokens(newAt, newRt);
    }

    @Transactional
    public void logout(AuthDto.LogoutReq req) {
        rtService.resolveByRawToken(req.getRefreshToken())
                .ifPresent(e -> rtService.revoke(e.sub, e.jti, e.hashHex));
    }
}