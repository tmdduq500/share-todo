package com.osy.sharetodo.feature.invitation.repository;

import com.osy.sharetodo.feature.invitation.domain.Invitation;
import com.osy.sharetodo.feature.invitation.domain.InvitationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByUid(String uid);
    Optional<Invitation> findByTokenHash(byte[] tokenHash);
    Optional<Invitation> findByEvent_IdAndChannelAndContactHash(Long eventId, InvitationChannel channel, byte[] contactHash);
}
