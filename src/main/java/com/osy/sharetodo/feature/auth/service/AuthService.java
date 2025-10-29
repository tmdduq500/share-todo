package com.osy.sharetodo.feature.auth.service;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.auth.dto.AuthDto;
import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import com.osy.sharetodo.global.security.JwtProps;
import com.osy.sharetodo.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
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
        String fam = UUID.randomUUID().toString();
        String jti = UUID.randomUUID().toString();
        String rt = jti;

        rtService.store(
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
        var found = rtService.resolveByRawToken(req.getRefreshToken())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

        if (rtService.isFamilyRevoked(found.sub, found.fam)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Session revoked");
        }

        rtService.revoke(found.sub, found.jti, found.hashHex);

        String newAt = jwtProvider.createAccessToken(found.sub);
        String newJti = UUID.randomUUID().toString();
        String newRt = newJti;

        rtService.store(
                found.sub,
                newJti,
                newRt,
                found.fam,
                Duration.ofDays(jwtProps.getRefreshTokenTtlDays())
        );

        return new AuthDto.Tokens(newAt, newRt);
    }

    @Transactional
    public void logout(AuthDto.LogoutReq req) {
        rtService.resolveByRawToken(req.getRefreshToken())
                .ifPresent(e -> rtService.revoke(e.sub, e.jti, e.hashHex));
    }
}