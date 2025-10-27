package com.osy.sharetodo.feature.participant.repository;

import com.osy.sharetodo.feature.participant.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByEvent_IdAndPerson_Id(Long eventId, Long personId);
    Optional<Participant> findByEvent_IdAndContactHash(Long eventId, byte[] contactHash);
}
