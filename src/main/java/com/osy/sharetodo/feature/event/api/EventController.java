package com.osy.sharetodo.feature.event.api;

import com.osy.sharetodo.feature.event.dto.EventDto;
import com.osy.sharetodo.feature.event.service.EventService;
import com.osy.sharetodo.global.response.ApiResponse;
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
        // JWT 서브젝트(accountUid)에서 가져오기 (SecurityFilter에서 설정되어 있다고 가정)
        String accountUid = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ApiResponse.ok(eventService.create(accountUid, req));
    }

    @GetMapping("/{uid}")
    public ApiResponse<EventDto.EventRes> get(@PathVariable String uid) {
        return ApiResponse.ok(eventService.getByUid(uid));
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return Map.of(
                "authenticated", auth != null && auth.isAuthenticated(),
                "principal", auth != null ? auth.getPrincipal() : null
        );
    }
}