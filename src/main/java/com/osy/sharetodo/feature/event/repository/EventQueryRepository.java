package com.osy.sharetodo.feature.event.repository;

import com.osy.sharetodo.feature.event.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface EventQueryRepository {
    Page<Event> searchByOwnerAndFilters(Long ownerPersonId, LocalDateTime fromUtc, LocalDateTime toUtc, String keyword, Pageable pageable);

    Page<Event> searchByParticipantAndFilters(Long personId, LocalDateTime fromUtc, LocalDateTime toUtc, String keyword, Pageable pageable);

    List<Event> searchCalendarEvents(Long personId, LocalDateTime fromUtc, LocalDateTime toUtc, String keyword);

}
