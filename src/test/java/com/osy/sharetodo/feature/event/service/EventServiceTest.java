package com.osy.sharetodo.feature.event.service;
import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.event.domain.Visibility;
import com.osy.sharetodo.feature.event.dto.EventDto;
import com.osy.sharetodo.feature.event.repository.EventRepository;
import com.osy.sharetodo.feature.person.domain.Person;
import com.osy.sharetodo.feature.person.repository.PersonRepository;
import com.osy.sharetodo.global.util.Ulids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventServiceTest {

    EventRepository eventRepository = mock(EventRepository.class);
    AccountRepository accountRepository = mock(AccountRepository.class);
    PersonRepository personRepository = mock(PersonRepository.class);
    Ulids ulids = mock(Ulids.class);

    EventService service;

    @BeforeEach
    void setUp() {
        service = new EventService(eventRepository, accountRepository, personRepository, ulids);
        when(ulids.newUlid()).thenReturn("01ABCULIDOWNER", "01ABCULIDEVENT"); // 순차 리턴
    }

    @Test
    void create_converts_local_to_utc_and_saves() {
        // given
        Account acc = Account.builder().id(1L).uid("ACCULID").emailNorm("user@example.com").build();
        when(accountRepository.findByUid("ACCULID")).thenReturn(Optional.of(acc));
        when(personRepository.findByAccount_Id(1L)).thenReturn(List.of());

        Event saved = Event.builder()
                .uid("01ABCULIDEVENT")
                .title("회의")
                .startsAtUtc(LocalDateTime.of(2025, 11, 1, 5, 0))
                .endsAtUtc(LocalDateTime.of(2025, 11, 1, 6, 0))
                .timezone("Asia/Seoul")
                .visibility(Visibility.PRIVATE)
                .build();
        when(eventRepository.save(any(Event.class))).thenReturn(saved);

        EventDto.EventCreateReq req = new EventDto.EventCreateReq();
        req.setTitle("회의");
        req.setDescription("설명");
        req.setStartAtLocal("2025-11-01T14:00:00");
        req.setEndAtLocal("2025-11-01T15:00:00");
        req.setTimezone("Asia/Seoul");
        req.setLocation("온라인");
        req.setAllDay(false);
        req.setVisibility(Visibility.PRIVATE);


        // when
        EventDto.EventRes res = service.create("ACCULID", req);
        Instant start = OffsetDateTime.parse(res.getStartsAtUtc()).toInstant();
        Instant end   = OffsetDateTime.parse(res.getEndsAtUtc()).toInstant();

        // then
        assertThat(res.getUid()).isEqualTo("01ABCULIDEVENT");
        assertThat(start).isEqualTo(Instant.parse("2025-11-01T05:00:00Z"));
        assertThat(end).isEqualTo(Instant.parse("2025-11-01T06:00:00Z"));
        verify(personRepository).save(any(Person.class));
        verify(eventRepository).save(argThat(e ->
                e.getTitle().equals("회의") &&
                        e.getTimezone().equals("Asia/Seoul") &&
                        e.getStartsAtUtc().equals(LocalDateTime.of(2025,11,1,5,0)) &&
                        e.getEndsAtUtc().equals(LocalDateTime.of(2025,11,1,6,0))
        ));
    }

    @Test
    void getByUid_ok() {
        Event e = Event.builder()
                .uid("EVTULID")
                .title("demo")
                .startsAtUtc(LocalDateTime.of(2025, 1, 1, 0, 0))
                .endsAtUtc(LocalDateTime.of(2025, 1, 1, 1, 0))
                .timezone("UTC")
                .visibility(Visibility.PRIVATE)
                .build();
        when(eventRepository.findByUid("EVTULID")).thenReturn(Optional.of(e));

        EventDto.EventRes res = service.getByUid("EVTULID");
        Instant i = OffsetDateTime.parse(res.getStartsAtUtc()).toInstant();
        LocalDateTime ldtUtc = i.atOffset(ZoneOffset.UTC).toLocalDateTime();
        assertThat(res.getTitle()).isEqualTo("demo");
        assertThat(ldtUtc).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
    }
}
