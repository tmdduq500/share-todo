package com.osy.sharetodo.feature.event.dto;

import com.osy.sharetodo.feature.event.domain.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventListRes {
    private String uid;
    private String title;
    private String startsAtUtc;
    private String endsAtUtc;
    private String location;
    private Visibility visibility;
}