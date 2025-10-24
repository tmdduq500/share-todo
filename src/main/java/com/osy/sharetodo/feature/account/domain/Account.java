package com.osy.sharetodo.feature.account.domain;

import com.osy.sharetodo.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 26, nullable = false, unique = true)
    private String uid; // ULID

    @Column(name = "email_norm", unique = true)
    private String emailNorm;

    @Column(name = "phone_norm", unique = true)
    private String phoneNorm;

    @Column(name = "password_hash")
    private byte[] passwordHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "last_login_at", columnDefinition = "datetime(6)")
    private LocalDateTime lastLoginAt;
}