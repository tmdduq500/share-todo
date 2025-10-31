package com.osy.sharetodo.global.util;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.auth.dto.AuthDto;
import com.osy.sharetodo.feature.auth.service.AuthService;
import com.osy.sharetodo.feature.auth.service.RefreshTokenService;
import com.osy.sharetodo.global.security.JwtProps;
import com.osy.sharetodo.global.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AccountRepository accountRepository;
    private PasswordEncoder passwordEncoder;
    private RefreshTokenService rtService;
    private JwtProps props;
    private JwtProvider jwtProvider;
    private AuthService authService;
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        rtService = mock(RefreshTokenService.class);

        props = new JwtProps();
        props.setIssuer("share-todo");
        props.setSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        props.setAccessTokenTtlMinutes(5);
        props.setRefreshTokenTtlDays(14);
        props.setInviteTokenTtlHours(168);

        jwtProvider = new JwtProvider(props);
        ReflectionTestUtils.invokeMethod(jwtProvider, "init");

        authService = new AuthService(accountRepository, passwordEncoder, jwtProvider, props, rtService, redisTemplate);
    }

    @Test
    void login_success_returns_jwt_at_and_uuid_rt() {
        // given
        Account acc = Account.builder()
                .uid("01HZYV6N4YGA6K93Q9X9A7CXDQ")
                .passwordHash("$2a$10$abcdefghijklmnopqrstuvwxyzABCDE".getBytes(StandardCharsets.UTF_8))
                .build();
        when(accountRepository.findByEmailNorm("user@example.com")).thenReturn(Optional.of(acc));
        when(passwordEncoder.matches(eq("pw"), anyString())).thenReturn(true);

        AuthDto.LoginReq req = new AuthDto.LoginReq();
        req.setEmail("user@example.com");
        req.setPassword("pw");

        // when
        AuthDto.Tokens tokens = authService.login(req);

        // then: AT는 비어있지 않고, RT는 UUID 포맷
        assertThat(tokens.getAccessToken()).isNotBlank();
        assertThat(tokens.getRefreshToken()).matches("^[0-9a-fA-F\\-]{36}$");

        // Redis store 호출 확인 (sub, jti=rt, fam 존재)
        ArgumentCaptor<String> subCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jtiCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> rawRtCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> famCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> ttlCap = ArgumentCaptor.forClass(Duration.class);

        verify(rtService).store(subCap.capture(), jtiCap.capture(), rawRtCap.capture(),
                famCap.capture(), ttlCap.capture());

        assertThat(subCap.getValue()).isEqualTo("01HZYV6N4YGA6K93Q9X9A7CXDQ");
        assertThat(jtiCap.getValue()).isEqualTo(tokens.getRefreshToken());
        assertThat(rawRtCap.getValue()).isEqualTo(tokens.getRefreshToken());

        assertThat(famCap.getValue()).matches("^[0-9a-fA-F\\-]{36}$");
        assertThat(ttlCap.getValue()).isEqualTo(Duration.ofDays(14));
    }

    @Test
    void refresh_rotates_uuid_rt_and_revokes_old() {
        // given: 기존 RT 가정
        String sub = "01HZYV6N4YGA6K93Q9X9A7CXDQ";
        String fam = UUID.randomUUID().toString();
        String oldJti = UUID.randomUUID().toString();
        String oldRt = oldJti;

        RefreshTokenService.Entry entry =
                new RefreshTokenService.Entry(sub, oldJti, fam, "hashhex");
        when(rtService.resolveByRawToken(oldRt)).thenReturn(Optional.of(entry));
        when(rtService.isFamilyRevoked(sub, fam)).thenReturn(false);

        AuthDto.RefreshReq req = new AuthDto.RefreshReq();
        req.setRefreshToken(oldRt);

        // when
        AuthDto.Tokens rotated = authService.refresh(req);

        // then: 새로운 UUID RT 발급, 이전 RT 폐기
        assertThat(rotated.getAccessToken()).isNotBlank();
        assertThat(rotated.getRefreshToken()).matches("^[0-9a-fA-F\\-]{36}$");
        assertThat(rotated.getRefreshToken()).isNotEqualTo(oldRt);

        verify(rtService).revoke(eq(sub), eq(oldJti), eq("hashhex"));
        // 새 store 호출 확인
        verify(rtService).store(eq(sub), matches("^[0-9a-fA-F\\-]{36}$"),
                eq(rotated.getRefreshToken()), eq(fam), eq(Duration.ofDays(14)));
    }
}