package com.osy.sharetodo.feature.invitation.repository;

import com.osy.sharetodo.feature.invitation.domain.Invitation;
import com.osy.sharetodo.feature.invitation.domain.InvitationChannel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByUid(String uid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Invitation> findByTokenHash(byte[] tokenHash);

    Optional<Invitation> findByEvent_IdAndChannelAndContactHash(Long eventId, InvitationChannel channel, byte[] contactHash);
}
