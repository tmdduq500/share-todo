package com.osy.sharetodo.feature.calendar;


import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.event.domain.Visibility;
import com.osy.sharetodo.feature.person.domain.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class IcsBuilderTest {
    @Test
    void build_single_event_ics() {
        Event e = Event.builder()
                .uid("EVTULID123")
                .owner(new Person())
                .title("회의")
                .description("상세 설명")
                .startsAtUtc(LocalDateTime.of(2025, 1, 1, 0, 0))
                .endsAtUtc(LocalDateTime.of(2025, 1, 1, 1, 0))
                .timezone("Asia/Seoul")
                .visibility(Visibility.PRIVATE)
                .build();

        String ics = IcsBuilder.singleEvent(e);
        assertThat(ics).contains("BEGIN:VCALENDAR", "BEGIN:VEVENT", "SUMMARY:회의", "DTSTART:20250101T000000Z");
        assertThat(ics).endsWith("END:VCALENDAR\r\n");
    }
}