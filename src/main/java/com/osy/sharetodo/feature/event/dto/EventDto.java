package com.osy.sharetodo.feature.event.dto;

import com.osy.sharetodo.feature.event.domain.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public final class EventDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class EventCreateReq {
        @NotBlank
        @Size(max = 200)
        private String title;

        private String description;

        @NotBlank
        private String startAtLocal;

        @NotBlank
        private String endAtLocal;

        @NotBlank
        private String timezone;

        private String location;

        private Boolean allDay = false;

        @NotNull
        private Visibility visibility = Visibility.PRIVATE;
    }


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventRes {
        private String uid;
        private String title;
        private String description;
        private String startsAtUtc;
        private String endsAtUtc;
        private String timezone;
        private String location;
        private boolean allDay;
        private Visibility visibility;
    }
}
