package com.osy.sharetodo.feature.event.domain;


import com.osy.sharetodo.feature.person.domain.Person;
import com.osy.sharetodo.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "event")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 26, nullable = false, unique = true)
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_person_id", nullable = false)
    private Person owner;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    private String description;

    @Column(name = "starts_at_utc", nullable = false, columnDefinition = "datetime(6)")
    private LocalDateTime startsAtUtc;

    @Column(name = "ends_at_utc", nullable = false, columnDefinition = "datetime(6)")
    private LocalDateTime endsAtUtc;

    private String timezone;

    private String location;

    @Column(name = "all_day", nullable = false)
    private boolean allDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

}