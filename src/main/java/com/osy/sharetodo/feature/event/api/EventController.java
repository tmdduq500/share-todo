package com.osy.sharetodo.feature.event.api;

import com.osy.sharetodo.feature.event.dto.EventDto;
import com.osy.sharetodo.feature.event.dto.EventListCondition;
import com.osy.sharetodo.feature.event.dto.EventListRes;
import com.osy.sharetodo.feature.event.service.EventService;
import com.osy.sharetodo.global.response.ApiResponse;
import com.osy.sharetodo.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ApiResponse<EventDto.EventRes> create(@Valid @RequestBody EventDto.EventCreateReq req) {
        String accountUid = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ApiResponse.ok(eventService.create(accountUid, req));
    }

    @GetMapping("/{uid}")
    public ApiResponse<EventDto.EventRes> get(@PathVariable String uid) {
        return ApiResponse.ok(eventService.getByUid(uid));
    }

    @GetMapping
    public ApiResponse<PageResponse<EventListRes>> list(@Valid EventListCondition eventListCondition) {
        String accountUid = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ApiResponse.ok(eventService.list(accountUid, eventListCondition));
    }

    @GetMapping("/invite")
    public ApiResponse<PageResponse<EventListRes>> invitedList(@Valid EventListCondition eventListCondition) {
        String accountUid = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ApiResponse.ok(eventService.invitedList(accountUid, eventListCondition));
    }
}