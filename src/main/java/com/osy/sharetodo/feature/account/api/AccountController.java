package com.osy.sharetodo.feature.account.api;

import com.osy.sharetodo.feature.account.dto.AccountDto;
import com.osy.sharetodo.feature.account.service.AccountService;
import com.osy.sharetodo.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}