package com.osy.sharetodo.feature.event.repository;

import com.osy.sharetodo.feature.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByUid(String uid);
}
