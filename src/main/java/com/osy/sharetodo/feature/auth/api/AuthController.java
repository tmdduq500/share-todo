package com.osy.sharetodo.feature.auth.api;

import com.osy.sharetodo.feature.auth.dto.AuthDto;
import com.osy.sharetodo.feature.auth.service.AuthService;
import com.osy.sharetodo.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthDto.Tokens> login(@Valid @RequestBody AuthDto.LoginReq req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthDto.Tokens> refresh(@Valid @RequestBody AuthDto.RefreshReq req) {
        return ApiResponse.ok(authService.refresh(req));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody AuthDto.LogoutReq req) {
        authService.logout(req);
        return ApiResponse.ok(null);
    }
}
