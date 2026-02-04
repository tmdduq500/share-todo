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
    private Person person; // 비회원이면 null

    @Column(name = "contact_hash", columnDefinition = "binary(32)")
    private byte[] contactHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status;

    /** 정적 생성자 (비회원 초대용) */
    public static Participant invite(Event event, byte[] contactHash) {
        Participant p = new Participant();
        p.event = event;
        p.contactHash = contactHash;
        p.role = ParticipantRole.ATTENDEE;
        p.status = ParticipantStatus.INVITED;
        return p;
    }

    /** 회원 기반 참가자 */
    public static Participant of(Event event, Person person, ParticipantRole role) {
        Participant p = new Participant();
        p.event = event;
        p.person = person;
        p.contactHash = null;
        p.role = role;
        p.status = ParticipantStatus.ACCEPTED;
        return p;
    }

    /** 초대 수락 처리 */
    public void accept() {
        this.status = ParticipantStatus.ACCEPTED;
    }

    /** 참가자(person) 연결 */
    public void updatePerson(Person person) {
        this.person = person;
    }
}

