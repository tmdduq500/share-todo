package com.osy.sharetodo.feature.contact.domain;


import com.osy.sharetodo.feature.person.domain.Person;
import com.osy.sharetodo.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "contact",
        uniqueConstraints = @UniqueConstraint(name = "uq_contact_channel_hash", columnNames = {"channel", "value_hash"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactChannel channel;

    @Column(name = "value_norm")
    private String valueNorm;

    @Column(name = "value_hash", nullable = false, columnDefinition = "binary(32)")
    private byte[] valueHash;

    @Column(name = "verified_at", columnDefinition = "datetime(6)")
    private LocalDateTime verifiedAt;

}