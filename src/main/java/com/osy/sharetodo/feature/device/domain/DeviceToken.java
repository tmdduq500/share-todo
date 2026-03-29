package com.osy.sharetodo.feature.device.domain;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "device_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_device_token_token", columnNames = "token")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevicePlatform platform;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private boolean active;

    public static DeviceToken create(Account account, String token, DevicePlatform platform) {
        return DeviceToken.builder()
                .account(account)
                .token(token)
                .platform(platform)
                .active(true)
                .build();
    }

    public void reassign(Account account, DevicePlatform platform) {
        this.account = account;
        this.platform = platform;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}