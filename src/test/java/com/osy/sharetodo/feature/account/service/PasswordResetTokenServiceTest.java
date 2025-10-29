package com.osy.sharetodo.feature.account.service;


import com.osy.sharetodo.global.util.Hashing;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class PasswordResetTokenServiceTest {

    @Test
    void issue_and_consume() {
        // given
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);

        Hashing hashing = new Hashing(); // pepper null-safe 버전
        PasswordResetTokenService s = new PasswordResetTokenService(redis, hashing);

        // when
        String token = s.issue("user@example.com", Duration.ofMinutes(30));

        // then
        assertThat(token).isNotBlank().hasSizeBetween(40, 50);

        String hex = s.hashHex(token);
        assertThat(hex).hasSize(64);

        verify(valueOps).set(
                startsWith("auth:prhash:"), eq("user@example.com"), any(Duration.class)
        );
    }
}