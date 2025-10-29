package com.osy.sharetodo.feature.account.service;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.dto.AccountDto;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.person.repository.PersonRepository;
import com.osy.sharetodo.global.util.Ulids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountServiceChangePasswordTest {

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
        service = new AccountService(accountRepository, personRepository, passwordEncoder, ulids, prt, mail, props, tmpl);
    }

    @Test
    void change_ok() {
        Account acc = Account.builder().uid("ACC").build();
        acc.changePassword("$old".getBytes(StandardCharsets.UTF_8));
        when(accountRepository.findByUid("ACC")).thenReturn(Optional.of(acc));
        when(passwordEncoder.matches("curPw", "$old")).thenReturn(true);
        when(passwordEncoder.encode("NewPw1!")).thenReturn("$new");

        AccountDto.ChangePasswordReq req = new AccountDto.ChangePasswordReq();
        req.setCurrentPassword("curPw");
        req.setNewPassword("NewPw1!");

        service.changePassword("ACC", req);

        assertThat(new String(acc.getPasswordHash(), StandardCharsets.UTF_8)).isEqualTo("$new");
    }

    @Test
    void change_fail_wrong_current() {
        Account acc = Account.builder().uid("ACC").build();
        acc.changePassword("$old".getBytes(StandardCharsets.UTF_8));
        when(accountRepository.findByUid("ACC")).thenReturn(Optional.of(acc));
        when(passwordEncoder.matches("curPw", "$old")).thenReturn(false);

        AccountDto.ChangePasswordReq req = new AccountDto.ChangePasswordReq();
        req.setCurrentPassword("curPw");
        req.setNewPassword("NewPw1!");

        assertThatThrownBy(() -> service.changePassword("ACC", req))
                .hasMessageContaining("현재 비밀번호가 일치하지");
    }
}