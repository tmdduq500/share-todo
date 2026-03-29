package com.osy.sharetodo.feature.device.repository;

import com.osy.sharetodo.feature.device.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByToken(String token);

    Optional<DeviceToken> findByAccount_uidAndToken(String accountUid, String token);

    List<DeviceToken> findAllByAccount_uidAndActiveTrue(String accountUid);
}
