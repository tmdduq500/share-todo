package com.osy.sharetodo.feature.account.repository;

import com.osy.sharetodo.feature.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUid(String uid);
    Optional<Account> findByEmailNorm(String emailNorm);
}