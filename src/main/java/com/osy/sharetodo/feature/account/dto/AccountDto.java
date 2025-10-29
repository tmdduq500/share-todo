package com.osy.sharetodo.feature.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class AccountDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SignupReq {
        @NotBlank
        @Email
        @Size(max = 255)
        private String email;

        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\p{Punct}]).{8,100}$",
                message = "비밀번호는 영문/숫자/특수문자를 포함해야 합니다.")
        private String password;

        @Size(max = 50)
        private String displayName; // 옵션
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SignupRes {
        private String accountUid;
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ChangePasswordReq {
        @NotBlank
        private String currentPassword;
        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\p{Punct}]).{8,100}$")
        private String newPassword;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ResetRequestReq {
        @NotBlank
        @Email
        @Size(max = 255)
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ResetConfirmReq {
        @NotBlank
        private String token;      // 이메일로 받은 토큰 원문
        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\p{Punct}]).{8,100}$")
        private String newPassword;
    }
}