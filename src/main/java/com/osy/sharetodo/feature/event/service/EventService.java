package com.osy.sharetodo.feature.event.service;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.event.dto.EventDto;
import com.osy.sharetodo.feature.event.dto.EventListCondition;
import com.osy.sharetodo.feature.event.dto.EventListRes;
import com.osy.sharetodo.feature.event.repository.EventRepository;
import com.osy.sharetodo.feature.person.domain.Person;
import com.osy.sharetodo.feature.person.repository.PersonRepository;
import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import com.osy.sharetodo.global.response.PageResponse;
import com.osy.sharetodo.global.util.TimeUtils;
import com.osy.sharetodo.global.util.Ulids;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;
    private final Ulids ulids;

    @Transactional
    public EventDto.EventRes create(String accountUid, EventDto.EventCreateReq req) {
        // 1) 계정 확인
        Account acc = accountRepository.findByUid(accountUid)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "계정을 찾을 수 없습니다."));

        // 2) 소유자 Person 확보(없으면 생성)
        Person owner = personRepository.findByAccount_Id(acc.getId())
                .orElseGet(() -> {
                    Person p = Person.builder()
                            .uid(ulids.newUlid())
                            .account(acc)
                            .displayName(acc.getEmailNorm())
                            .build();
                    return personRepository.save(p);
                });

        // 3) 시간 변환 (로컬+TZ → UTC)
        LocalDateTime startsUtc = TimeUtils.toUtcFromIsoZ(req.getStartAtLocal(), req.getTimezone());
        LocalDateTime endsUtc = TimeUtils.toUtcFromIsoZ(req.getEndAtLocal(), req.getTimezone());
        if (!endsUtc.isAfter(startsUtc)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "종료 시각이 시작 시각 이후여야 합니다.");
        }

        // 4) 저장
        Event e = Event.builder()
                .uid(ulids.newUlid())
                .owner(owner)
                .title(req.getTitle())
                .description(req.getDescription())
                .startsAtUtc(startsUtc)
                .endsAtUtc(endsUtc)
                .timezone(req.getTimezone())
                .location(req.getLocation())
                .allDay(Boolean.TRUE.equals(req.getAllDay()))
                .visibility(req.getVisibility())
                .build();
        Event saved = eventRepository.save(e);

        // 5) 응답
        EventDto.EventRes res = EventDto.EventRes.builder()
                .uid(saved.getUid())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .startsAtUtc(TimeUtils.toIsoZ(saved.getStartsAtUtc()))
                .endsAtUtc(TimeUtils.toIsoZ(saved.getEndsAtUtc()))
                .timezone(saved.getTimezone())
                .location(saved.getLocation())
                .allDay(saved.isAllDay())
                .visibility(saved.getVisibility())
                .build();
        return res;
    }

    public EventDto.EventRes getByUid(String uid) {
        Event e = eventRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "이벤트를 찾을 수 없습니다."));
        EventDto.EventRes res = EventDto.EventRes.builder()
                .uid(e.getUid())
                .title(e.getTitle())
                .description(e.getDescription())
                .startsAtUtc(TimeUtils.toIsoZ(e.getStartsAtUtc()))
                .endsAtUtc(TimeUtils.toIsoZ(e.getEndsAtUtc()))
                .timezone(e.getTimezone())
                .location(e.getLocation())
                .allDay(e.isAllDay())
                .visibility(e.getVisibility())
                .build();
        return res;
    }

    public PageResponse<EventListRes> list(String accountUid, EventListCondition eventListCondition) {
        Account acc = accountRepository.findByUid(accountUid)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "계정을 찾을 수 없습니다."));

        Person owner = personRepository.findByAccount_Id(acc.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR, "소유자 정보를 찾을 수 없습니다."));

        // 기간 변환
        LocalDateTime fromUtc = null, toUtc = null;
        if (eventListCondition.getFromLocal() != null && eventListCondition.getTimezone() != null) {
            fromUtc = TimeUtils.toUtc(eventListCondition.getFromLocal(), eventListCondition.getTimezone());
        }
        if (eventListCondition.getToLocal() != null && eventListCondition.getTimezone() != null) {
            toUtc = TimeUtils.toUtc(eventListCondition.getToLocal(), eventListCondition.getTimezone());
        }

        int page = eventListCondition.getPage() == null ? 0 : Math.max(0, eventListCondition.getPage());
        int size = eventListCondition.getSize() == null ? 20 : Math.min(100, Math.max(1, eventListCondition.getSize()));
        Sort sort = Sort.by(Sort.Direction.ASC, "startsAtUtc"); // 고정
        Pageable pageable = PageRequest.of(page, size, sort);

        var pageEvents = eventRepository.searchByOwnerAndFilters(
                owner.getId(),
                fromUtc, toUtc,
                eventListCondition.getQ(),
                pageable
        );

        // 매핑
        var mapped = pageEvents.map(e -> {
            EventListRes r = new EventListRes();
            r.setUid(e.getUid());
            r.setTitle(e.getTitle());
            r.setDescription(e.getDescription());
            r.setStartsAtUtc(e.getStartsAtUtc().atOffset(java.time.ZoneOffset.UTC).toString());
            r.setEndsAtUtc(e.getEndsAtUtc().atOffset(java.time.ZoneOffset.UTC).toString());
            r.setLocation(e.getLocation());
            r.setVisibility(e.getVisibility());
            return r;
        });

        return PageResponse.of(mapped);
    }

    public PageResponse<EventListRes> invitedList(String accountUid, EventListCondition eventListCondition) {
        Account acc = accountRepository.findByUid(accountUid)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "계정을 찾을 수 없습니다."));

        Person me = personRepository.findByAccount_Id(acc.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR, "사용자 정보를 찾을 수 없습니다."));

        LocalDateTime fromUtc = null, toUtc = null;
        if (eventListCondition.getFromLocal() != null && eventListCondition.getTimezone() != null) {
            fromUtc = TimeUtils.toUtc(eventListCondition.getFromLocal(), eventListCondition.getTimezone());
        }
        if (eventListCondition.getToLocal() != null && eventListCondition.getTimezone() != null) {
            toUtc = TimeUtils.toUtc(eventListCondition.getToLocal(), eventListCondition.getTimezone());
        }

        int page = eventListCondition.getPage() == null ? 0 : Math.max(0, eventListCondition.getPage());
        int size = eventListCondition.getSize() == null ? 20 : Math.min(100, Math.max(1, eventListCondition.getSize()));
        Sort sort = Sort.by(Sort.Direction.ASC, "startsAtUtc");
        Pageable pageable = PageRequest.of(page, size, sort);

        var pageEvents = eventRepository.searchByParticipantAndFilters(
                me.getId(),
                fromUtc, toUtc,
                eventListCondition.getQ(),
                pageable
        );

        var mapped = pageEvents.map(e -> {
            EventListRes r = new EventListRes();
            r.setUid(e.getUid());
            r.setTitle(e.getTitle());
            r.setStartsAtUtc(e.getStartsAtUtc().atOffset(java.time.ZoneOffset.UTC).toString());
            r.setEndsAtUtc(e.getEndsAtUtc().atOffset(java.time.ZoneOffset.UTC).toString());
            r.setLocation(e.getLocation());
            r.setVisibility(e.getVisibility());
            return r;
        });

        return PageResponse.of(mapped);
    }


}