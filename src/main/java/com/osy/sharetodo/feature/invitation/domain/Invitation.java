package com.osy.sharetodo.feature.invitation.domain;


import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.person.domain.Person;
import com.osy.sharetodo.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "invitation",
        uniqueConstraints = @UniqueConstraint(name = "uq_invitation_unique",
                columnNames = {"event_id", "channel", "contact_hash", "accepted_at"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invitation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 26, nullable = false, unique = true)
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_person_id", nullable = false)
    private Person inviter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationChannel channel;

    @Column(name = "contact_hash", nullable = false, columnDefinition = "binary(32)")
    private byte[] contactHash;

    @Column(name = "token_hash", nullable = false, columnDefinition = "binary(32)")
    private byte[] tokenHash;

    @Column(name = "expires_at", columnDefinition = "datetime(6)", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at", columnDefinition = "datetime(6)")
    private LocalDateTime acceptedAt;

}