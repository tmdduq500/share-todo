package com.osy.sharetodo.feature.event.service;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.event.domain.Visibility;
import com.osy.sharetodo.feature.event.dto.EventListCondition;
import com.osy.sharetodo.feature.event.dto.EventListRes;
import com.osy.sharetodo.feature.event.repository.EventRepository;
import com.osy.sharetodo.feature.person.domain.Person;
import com.osy.sharetodo.feature.person.repository.PersonRepository;
import com.osy.sharetodo.global.response.PageResponse;
import com.osy.sharetodo.global.util.Ulids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventServiceListTest {

    EventRepository eventRepository = mock(EventRepository.class);
    AccountRepository accountRepository = mock(AccountRepository.class);
    PersonRepository personRepository = mock(PersonRepository.class);
    Ulids ulids = mock(Ulids.class);

    EventService service;

    @BeforeEach
    void setUp() {
        service = new EventService(eventRepository, accountRepository, personRepository, ulids);
    }

    @Test
    void list_filters_and_maps_page() {
        // given
        Account acc = Account.builder().id(10L).uid("ACCULID").emailNorm("dev@example.com").build();
        when(accountRepository.findByUid("ACCULID")).thenReturn(Optional.of(acc));

        Person owner = Person.builder()
                .id(100L)
                .build();
        when(personRepository.findByAccount_Id(10L)).thenReturn(Optional.ofNullable(owner));

        Event e1 = Event.builder()
                .uid("EVT1")
                .title("회의A")
                .startsAtUtc(LocalDateTime.of(2025, 11, 1, 5, 0))
                .endsAtUtc(LocalDateTime.of(2025, 11, 1, 6, 0))
                .location("온라인")
                .visibility(Visibility.PRIVATE)
                .build();

        Page<Event> page = new PageImpl<>(List.of(e1), PageRequest.of(0, 10), 1);
        when(eventRepository.searchByOwnerAndFilters(eq(100L), any(), any(), eq("회의"), any(Pageable.class)))
                .thenReturn(page);

        EventListCondition q = new EventListCondition();
        q.setFromLocal("2025-11-01T00:00:00");
        q.setToLocal("2025-11-30T23:59:59");
        q.setTimezone("Asia/Seoul");
        q.setQ("회의");
        q.setPage(0); q.setSize(10);

        // when
        PageResponse<EventListRes> res = service.list("ACCULID", q);

        // then
        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().get(0).getUid()).isEqualTo("EVT1");
        assertThat(res.getTotalElements()).isEqualTo(1);
        verify(eventRepository).searchByOwnerAndFilters(eq(100L), any(), any(), eq("회의"), any(Pageable.class));
    }
}