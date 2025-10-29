package com.osy.sharetodo.global.config;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.domain.AccountStatus;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.person.domain.Person;
import com.osy.sharetodo.feature.person.repository.PersonRepository;
import com.osy.sharetodo.global.util.Ulids;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevBootstrap {

    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final Ulids ulids;

    @Transactional
    public void init() {
        if (accountRepository.count() > 0) return;

        String email = "dev@example.com";
        String rawPw = "dev1234";
        String hash = passwordEncoder.encode(rawPw);

        Account acc = Account.builder()
                .uid(ulids.newUlid())
                .emailNorm(email.toLowerCase())
                .passwordHash(hash.getBytes())
                .provider("local")
                .status(AccountStatus.ACTIVE)
                .build();
        acc = accountRepository.save(acc);

        Person p = Person.builder()
                .uid(ulids.newUlid())
                .account(acc)
                .displayName("dev")
                .build();
        personRepository.save(p);

        System.out.println("[DEV-BOOTSTRAP] seeded account: " + email + " / pw: " + rawPw);
    }

    @PostConstruct
    public void run() {
        init();
    }
}
