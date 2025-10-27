package com.osy.sharetodo.feature.invitation.api;

import com.osy.sharetodo.feature.invitation.dto.InvitationDto;
import com.osy.sharetodo.feature.invitation.service.InvitationService;
import com.osy.sharetodo.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping
    public ApiResponse<InvitationDto.CreateRes> create(@Valid @RequestBody InvitationDto.CreateReq req) {
        String accountUid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ApiResponse.ok(invitationService.create(req, accountUid));
    }

    @PostMapping("/accept")
    public ApiResponse<InvitationDto.AcceptRes> accept(@Valid @RequestBody InvitationDto.AcceptReq req) {
        return ApiResponse.ok(invitationService.accept(req));
    }
}