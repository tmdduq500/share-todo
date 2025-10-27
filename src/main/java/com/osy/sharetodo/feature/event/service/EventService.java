package com.osy.sharetodo.feature.event.service;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.event.dto.EventDto;
import com.osy.sharetodo.feature.event.repository.EventRepository;
import com.osy.sharetodo.feature.person.domain.Person;
import com.osy.sharetodo.feature.person.repository.PersonRepository;
import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import com.osy.sharetodo.global.util.TimeUtils;
import com.osy.sharetodo.global.util.Ulids;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
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
                .stream().findFirst().orElseGet(() -> {
                    Person p = Person.builder()
                            .uid(ulids.newUlid())
                            .account(acc)
                            .displayName(acc.getEmailNorm())
                            .build();
                    return personRepository.save(p);
                });

        // 3) 시간 변환 (로컬+TZ → UTC)
        LocalDateTime startsUtc = TimeUtils.toUtc(req.getStartAtLocal(), req.getTimezone());
        LocalDateTime endsUtc = TimeUtils.toUtc(req.getEndAtLocal(), req.getTimezone());
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

    @Transactional
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
}