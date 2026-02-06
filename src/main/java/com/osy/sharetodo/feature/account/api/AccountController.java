package com.osy.sharetodo.feature.account.api;

import com.osy.sharetodo.feature.account.dto.AccountDto;
import com.osy.sharetodo.feature.account.service.AccountService;
import com.osy.sharetodo.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/signup")
    public ApiResponse<AccountDto.SignupRes> signup(@Valid @RequestBody AccountDto.SignupReq req) {
        return ApiResponse.ok(accountService.signup(req));
    }

    @PostMapping("/password/change")
    public ApiResponse<Void> changePassword(@Valid @RequestBody AccountDto.ChangePasswordReq req) {
        String accountUid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        accountService.changePassword(accountUid, req);
        return ApiResponse.ok(null);
    }

    @PostMapping("/password/reset/request")
    public ApiResponse<Void> resetRequest(@Valid @RequestBody AccountDto.ResetRequestReq req) {
        accountService.resetRequest(req);
        return ApiResponse.ok(null);
    }

    @PostMapping("/password/reset/confirm")
    public ApiResponse<Void> resetConfirm(@Valid @RequestBody AccountDto.ResetConfirmReq req) {
        accountService.resetConfirm(req);
        return ApiResponse.ok(null);
    }

    @GetMapping("/exists")
    public ApiResponse<AccountDto.EmailExistsRes> emailExists(@RequestParam String email) {
        boolean exists = accountService.emailExists(email);
        return ApiResponse.ok(AccountDto.EmailExistsRes.of(exists));
    }

    @PostMapping("/verify/request")
    public ApiResponse<Void> requestEmailVerify(@Valid @RequestBody AccountDto.EmailVerifyRequestReq req) {
        accountService.sendVerifyCode(req.getEmail());
        return ApiResponse.ok(null);
    }

    @PostMapping("/verify/confirm")
    public ApiResponse<Void> confirmEmailVerify(@Valid @RequestBody AccountDto.EmailVerifyConfirmReq req) {
        accountService.confirmVerifyCode(req.getEmail(), req.getCode());
        return ApiResponse.ok(null);
    }
}