package com.osy.sharetodo.feature.notification.domain;

import com.osy.sharetodo.feature.invitation.domain.Invitation;
import com.osy.sharetodo.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false)
    private Invitation invitation;

    @Column(nullable = false)
    private String provider; // SES | TWILIO | SENS

    @Lob
    private String requestPayload;
    @Lob
    private String responsePayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private int retryCount;

}
