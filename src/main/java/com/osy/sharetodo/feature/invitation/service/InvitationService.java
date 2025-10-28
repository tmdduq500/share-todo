package com.osy.sharetodo.feature.invitation.service;

import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.event.repository.EventRepository;
import com.osy.sharetodo.feature.invitation.domain.Invitation;
import com.osy.sharetodo.feature.invitation.domain.InvitationChannel;
import com.osy.sharetodo.feature.invitation.dto.InvitationDto;
import com.osy.sharetodo.feature.invitation.repository.InvitationRepository;
import com.osy.sharetodo.feature.invitation.template.InvitationEmailTemplate;
import com.osy.sharetodo.feature.notification.mail.MailPort;
import com.osy.sharetodo.feature.participant.domain.Participant;
import com.osy.sharetodo.feature.participant.domain.ParticipantRole;
import com.osy.sharetodo.feature.participant.domain.ParticipantStatus;
import com.osy.sharetodo.feature.participant.repository.ParticipantRepository;
import com.osy.sharetodo.feature.person.domain.Person;
import com.osy.sharetodo.feature.person.repository.PersonRepository;
import com.osy.sharetodo.global.config.AppProps;
import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import com.osy.sharetodo.global.util.Ulids;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitationService {

    private final EventRepository eventRepository;
    private final InvitationRepository invitationRepository;
    private final ParticipantRepository participantRepository;
    private final InviteTokenService inviteTokenService;
    private final Ulids ulids;

    private final MailPort mailPort;
    private final AppProps appProps;
    private final InvitationEmailTemplate emailTemplate;

    private static String normalizeEmail(String email) {
        return StringUtils.trimToEmpty(email).toLowerCase();
    }
    private static String normalizePhone(String phone) {
        return StringUtils.trimToEmpty(phone).replaceAll("[^0-9+]", "");
    }

    private byte[] contactHash(InvitationChannel ch, String target) {
        String norm = ch == InvitationChannel.EMAIL ? normalizeEmail(target) : normalizePhone(target);
        return inviteTokenService.hash(ch.name() + ":" + norm);
    }

    @Transactional
    public InvitationDto.CreateRes create(InvitationDto.CreateReq req, String inviterAccountUid) {
        Event event = eventRepository.findByUid(req.getEventUid())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "이벤트를 찾을 수 없습니다."));

        // 참가자 생성
        byte[] cHash = contactHash(req.getChannel(), req.getTarget());
        Participant participant = participantRepository
                .findByEvent_IdAndContactHash(event.getId(), cHash)
                .orElseGet(() -> participantRepository.save(Participant.invite(event, cHash)));

        // 초대 토큰 생성
        String rawToken = inviteTokenService.newToken();
        byte[] tokenHash = inviteTokenService.hash(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC)
                .plusHours(Optional.ofNullable(req.getTtlHours()).orElse(168));

        Person inviter = event.getOwner();

        Invitation inv = Invitation.create(
                event, inviter, req.getChannel(), cHash, tokenHash, expiresAt, ulids.newUlid()
        );
        invitationRepository.save(inv);

        InvitationDto.CreateRes res = new InvitationDto.CreateRes();
        res.setInvitationUid(inv.getUid());
        res.setToken(rawToken);

        if (req.getChannel() == InvitationChannel.EMAIL) {
            String to = normalizeEmail(req.getTarget());
            String subject = emailTemplate.subject(event.getTitle());
            String body = emailTemplate.body(appProps.getBaseUrl(), rawToken, event.getTitle(), event.getDescription());
            mailPort.send(to, subject, body);
        }

        return res;
    }

    @Transactional
    public InvitationDto.AcceptRes accept(InvitationDto.AcceptReq req) {
        byte[] tokenHash = inviteTokenService.hash(req.getToken());
        Invitation inv = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "유효하지 않은 초대 토큰입니다."));

        if (inv.isExpired(LocalDateTime.now(ZoneOffset.UTC)))
            throw new ApiException(ErrorCode.UNAUTHORIZED, "초대 토큰이 만료되었습니다.");

        // 이미 수락된 초대는 멱등 처리
        if (inv.getAcceptedAt() != null)
            return makeAcceptedRes(inv.getEvent().getUid());

        Participant participant = participantRepository
                .findByEvent_IdAndContactHash(inv.getEvent().getId(), inv.getContactHash())
                .orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR, "참가자를 찾을 수 없습니다."));

        participant.accept();
        inv.accept();

        return makeAcceptedRes(inv.getEvent().getUid());
    }

    private InvitationDto.AcceptRes makeAcceptedRes(String eventUid) {
        InvitationDto.AcceptRes res = new InvitationDto.AcceptRes();
        res.setEventUid(eventUid);
        res.setStatus("ACCEPTED");
        return res;
    }

    /** 초대 토큰으로 이벤트 조회(ICS용) */
    public Event getEventByToken(String rawToken) {
        byte[] tokenHash = inviteTokenService.hash(rawToken);
        Invitation inv = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "유효하지 않은 초대 토큰입니다."));
        if (inv.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "초대 토큰이 만료되었습니다.");
        }
        return inv.getEvent();
    }
}