package com.osy.sharetodo.feature.person.domain;


import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "person")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 26, nullable = false, unique = true)
    private String uid; // ULID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private String displayName;

}