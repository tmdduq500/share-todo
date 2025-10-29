package com.osy.sharetodo.feature.account.service;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.dto.AccountDto;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.person.repository.PersonRepository;
import com.osy.sharetodo.global.util.Ulids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AccountServiceSignupTest {

    AccountRepository accountRepository = mock(AccountRepository.class);
    PersonRepository personRepository = mock(PersonRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    PasswordResetTokenService prt = mock(PasswordResetTokenService.class);
    com.osy.sharetodo.feature.notification.mail.MailPort mail = mock(com.osy.sharetodo.feature.notification.mail.MailPort.class);
    com.osy.sharetodo.global.config.AppProps props = new com.osy.sharetodo.global.config.AppProps();
    com.osy.sharetodo.feature.account.template.PasswordResetEmailTemplate tmpl =
            new com.osy.sharetodo.feature.account.template.PasswordResetEmailTemplate();
    Ulids ulids = mock(Ulids.class);

    AccountService service;

    @BeforeEach
    void setUp() {
        when(ulids.newUlid()).thenReturn("ULID-1", "ULID-2");
        service = new AccountService(accountRepository, personRepository, passwordEncoder, ulids, prt, mail, props, tmpl);
    }

    @Test
    void signup_ok() {
        when(accountRepository.findByEmailNorm("user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Aa1!aaaa")).thenReturn("$hash");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountDto.SignupReq req = new AccountDto.SignupReq();
        req.setEmail("User@Example.com");
        req.setPassword("Aa1!aaaa");
        req.setDisplayName("dev");

        var res = service.signup(req);

        assertThat(res.getAccountUid()).isEqualTo("ULID-1");
        assertThat(res.getEmail()).isEqualTo("user@example.com");
        verify(personRepository).save(any());
    }

    @Test
    void signup_conflict() {
        when(accountRepository.findByEmailNorm("user@example.com"))
                .thenReturn(Optional.of(Account.builder().build()));
        AccountDto.SignupReq req = new AccountDto.SignupReq();
        req.setEmail("user@example.com");
        req.setPassword("Aa1!aaaa");

        assertThatThrownBy(() -> service.signup(req))
                .hasMessageContaining("이미 가입된 이메일");
    }
}