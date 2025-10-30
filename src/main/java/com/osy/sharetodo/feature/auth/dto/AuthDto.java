package com.osy.sharetodo.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class AuthDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LoginReq {
        @NotBlank
        private String email;
        @NotBlank
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Tokens {
        private String accessToken;
        private String refreshToken;

        public Tokens(String access, String refresh) {
            this.accessToken = access;
            this.refreshToken = refresh;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RefreshReq {
        @NotBlank
        private String refreshToken;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LogoutReq {
        @NotBlank
        private String accessToken;
        @NotBlank
        private String refreshToken;
    }
}