package com.osy.sharetodo.global.security;

import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProps props;
    private Key key;

    @PostConstruct
    void init() {
        byte[] bytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes)");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    private JwtBuilder base(String subject, String tokenType, String jti, Instant iat, Instant exp) {
        return Jwts.builder()
                .setIssuer(props.getIssuer())
                .setSubject(subject)
                .claim("typ", tokenType)
                .setId(jti)
                .setIssuedAt(Date.from(iat))
                .setExpiration(Date.from(exp));
    }

    public String createAccessToken(String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + props.getAccessTokenTtlMinutes());
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setId(jti)
                .claim("typ", "access")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String accountUid, String jti, String famId) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getRefreshTokenTtlDays(), ChronoUnit.DAYS);
        return base(accountUid, "refresh", jti, now, exp)
                .claim("fam", famId)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(jwt);
    }

    public void assertType(Jws<Claims> jws, String expectedType) {
        Object typ = jws.getBody().get("typ");
        if (!(typ instanceof String) || !expectedType.equals(typ)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid token type");
        }
    }
}

