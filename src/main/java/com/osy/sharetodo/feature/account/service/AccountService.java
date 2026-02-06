package com.osy.sharetodo.feature.account.service;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.domain.AccountStatus;
import com.osy.sharetodo.feature.account.dto.AccountDto;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.account.template.PasswordResetEmailTemplate;
import com.osy.sharetodo.feature.notification.mail.MailPort;
import com.osy.sharetodo.feature.person.domain.Person;
import com.osy.sharetodo.feature.person.repository.PersonRepository;
import com.osy.sharetodo.global.config.AppProps;
import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import com.osy.sharetodo.global.util.Ulids;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final Ulids ulids;

    private final PasswordResetTokenService resetTokenService;
    private final EmailVerificationCodeService emailVerificationCodeService;
    private final MailPort mailPort;
    private final AppProps appProps;
    private final PasswordResetEmailTemplate resetTemplate;

    private static String normEmail(String email) {
        return StringUtils.trimToEmpty(email).toLowerCase(Locale.ROOT);
    }

    @Transactional
    public AccountDto.SignupRes signup(AccountDto.SignupReq req) {
        String emailNorm = normEmail(req.getEmail());

        if (!emailVerificationCodeService.isVerified(emailNorm)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "이메일 인증을 완료해주세요.");
        }

        if (accountRepository.findByEmailNorm(emailNorm).isPresent()) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 가입된 이메일입니다.");
        }
        String hashed = passwordEncoder.encode(req.getPassword()); // bcrypt 문자열

        Account acc = Account.builder()
                .uid(ulids.newUlid())
                .emailNorm(emailNorm)
                .passwordHash(hashed.getBytes(StandardCharsets.UTF_8))
                .provider("local")
                .status(AccountStatus.ACTIVE)
                .build();
        acc = accountRepository.save(acc);

        Person p = Person.builder()
                .uid(ulids.newUlid())
                .account(acc)
                .displayName(Optional.ofNullable(req.getDisplayName()).filter(StringUtils::isNotBlank).orElse(emailNorm))
                .build();
        personRepository.save(p);

        AccountDto.SignupRes res = new AccountDto.SignupRes();
        res.setAccountUid(acc.getUid());
        res.setEmail(acc.getEmailNorm());
        return res;
    }

    @Transactional
    public void changePassword(String accountUid, AccountDto.ChangePasswordReq req) {
        Account acc = accountRepository.findByUid(accountUid)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "계정을 찾을 수 없습니다."));

        String stored = acc.getPasswordHash() == null ? "" : new String(acc.getPasswordHash(), StandardCharsets.UTF_8);
        if (!passwordEncoder.matches(req.getCurrentPassword(), stored)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "현재 비밀번호가 일치하지 않습니다.");
        }
        String newHash = passwordEncoder.encode(req.getNewPassword());
        acc.changePassword(newHash.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional
    public void resetRequest(AccountDto.ResetRequestReq req) {
        String emailNorm = normEmail(req.getEmail());
        // 존재 여부 노출 방지: 계정이 없으면 그냥 성공 응답
        Optional<Account> found = accountRepository.findByEmailNorm(emailNorm);
        if (found.isEmpty()) return;

        String token = resetTokenService.issue(emailNorm, Duration.ofMinutes(30)); // 30분 유효
        String subject = resetTemplate.subject();
        String body = resetTemplate.body(appProps.getFrontBaseUrl(), appProps.getBackBaseUrl(), token, emailNorm);
        mailPort.send(emailNorm, subject, body);
    }

    @Transactional
    public void resetConfirm(AccountDto.ResetConfirmReq req) {
        String emailNorm = resetTokenService.consume(req.getToken());
        if (emailNorm == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다.");
        }
        Account acc = accountRepository.findByEmailNorm(emailNorm)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "계정을 찾을 수 없습니다."));

        String newHash = passwordEncoder.encode(req.getNewPassword());
        acc.changePassword(newHash.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        String emailNorm = normEmail(email);
        return accountRepository.findByEmailNorm(emailNorm).isPresent();
    }

    @Transactional
    public void sendVerifyCode(String email) {
        String emailNorm = normEmail(email);

        // 이미 가입된 이메일이면 인증 발송 막기
        if (accountRepository.findByEmailNorm(emailNorm).isPresent()) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 가입된 이메일입니다.");
        }

        String code = emailVerificationCodeService.issueCode(emailNorm);
        if (code == null) {
            throw new ApiException(ErrorCode.TOO_MANY_REQUESTS, "인증번호는 1분마다 발송 가능합니다.");
        }

        // 메일 발송
        String subject = "[ShareTodo] 이메일 인증번호";
        String body = "인증번호는 " + code + " 입니다.\n(5분 이내 입력)";
        mailPort.send(emailNorm, subject, body);
    }

    @Transactional
    public void confirmVerifyCode(String email, String code) {
        boolean ok = emailVerificationCodeService.confirm(email, code);
        if (!ok) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "인증번호가 올바르지 않거나 만료되었습니다.");
        }
    }

}
