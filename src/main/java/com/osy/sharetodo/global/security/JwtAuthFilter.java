package com.osy.sharetodo.global.security;

import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Jws<Claims> jws = jwtProvider.parse(token);
                jwtProvider.assertType(jws, "access");

                String jti = jws.getBody().getId();
                if (jti != null && Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:access:" + jti))) {
                    throw new ApiException(ErrorCode.UNAUTHORIZED, "이미 로그아웃된 토큰입니다.");
                }

                String accountUid = jws.getBody().getSubject();
                AbstractAuthenticationToken authentication = new AbstractAuthenticationToken(
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))) {
                    @Override
                    public Object getCredentials() {
                        return token;
                    }

                    @Override
                    public Object getPrincipal() {
                        return accountUid;
                    }
                };
                authentication.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {
            }
        }

        filterChain.doFilter(request, response);
    }
}
