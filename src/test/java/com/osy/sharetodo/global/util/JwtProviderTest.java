package com.osy.sharetodo.global.util;

import com.osy.sharetodo.global.security.JwtProps;
import com.osy.sharetodo.global.security.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class JwtProviderTest {

    private JwtProvider providerWithInit(String secret) {
        JwtProps props = new JwtProps();
        props.setIssuer("share-todo");
        props.setSecret(secret);
        props.setAccessTokenTtlMinutes(5);
        props.setRefreshTokenTtlDays(14);
        props.setInviteTokenTtlHours(168);

        JwtProvider provider = new JwtProvider(props);
        ReflectionTestUtils.invokeMethod(provider, "init");
        return provider;
    }

    @Test
    void create_and_parse_access_token_ok() {
        String strongSecret = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        JwtProvider provider = providerWithInit(strongSecret);

        String at = provider.createAccessToken("01HZYV6N4YGA6K93Q9X9A7CXDQ");
        Jws<Claims> parsed = provider.parse(at);

//        assertThat(parsed.getBody().getIssuer()).isEqualTo("share-todo");
        assertThat(parsed.getBody().getSubject()).isEqualTo("01HZYV6N4YGA6K93Q9X9A7CXDQ");
        assertThat(parsed.getBody().getExpiration()).isNotNull();
    }

    @Test
    void init_throws_when_secret_too_short() {
        JwtProps weak = new JwtProps();
        weak.setIssuer("share-todo");
        weak.setSecret("short-secret");
        weak.setAccessTokenTtlMinutes(5);
        weak.setRefreshTokenTtlDays(14);
        weak.setInviteTokenTtlHours(168);

        JwtProvider provider = new JwtProvider(weak);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(provider, "init"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("256 bits");
    }
}