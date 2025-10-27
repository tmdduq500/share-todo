package com.osy.sharetodo.feature.invitation.dto;

import com.osy.sharetodo.feature.invitation.domain.InvitationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class InvitationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateReq {
        @NotBlank
        private String eventUid;
        @NotNull
        private InvitationChannel channel;
        @NotBlank
        private String target;
        private Integer ttlHours;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateRes {
        private String invitationUid;
        private String token;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AcceptReq {
        @NotBlank
        private String token;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AcceptRes {
        private String eventUid;
        private String status;
    }
}