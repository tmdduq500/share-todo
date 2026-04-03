package com.osy.sharetodo.feature.participant.domain;


import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.person.domain.Person;
import com.osy.sharetodo.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "participant",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_participant_event_person", columnNames = {"event_id", "person_id"}),
                @UniqueConstraint(name = "uq_participant_event_contact", columnNames = {"event_id", "contact_hash"})
        })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "contact_hash", columnDefinition = "binary(32)")
    private byte[] contactHash;

    @Column(name = "email_norm", length = 320)
    private String emailNorm;

    @Column(name = "phone_norm", length = 30)
    private String phoneNorm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status;

    public static Participant inviteEmail(Event event, byte[] contactHash, String emailNorm) {
        Participant p = new Participant();
        p.event = event;
        p.contactHash = contactHash;
        p.emailNorm = emailNorm;
        p.role = ParticipantRole.ATTENDEE;
        p.status = ParticipantStatus.INVITED;
        return p;
    }

    public static Participant invitePhone(Event event, byte[] contactHash, String phoneNorm) {
        Participant p = new Participant();
        p.event = event;
        p.contactHash = contactHash;
        p.phoneNorm = phoneNorm;
        p.role = ParticipantRole.ATTENDEE;
        p.status = ParticipantStatus.INVITED;
        return p;
    }

    public static Participant of(Event event, Person person, ParticipantRole role) {
        Participant p = new Participant();
        p.event = event;
        p.person = person;
        p.contactHash = null;
        p.role = role;
        p.status = ParticipantStatus.ACCEPTED;
        return p;
    }

    public void accept() {
        this.status = ParticipantStatus.ACCEPTED;
    }

    public void updatePerson(Person person) {
        this.person = person;
    }
}

