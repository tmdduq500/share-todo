package com.osy.sharetodo.feature.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventListCondition {
    private String fromLocal;
    private String toLocal;
    private String timezone;
    private String q;
    private Integer page = 0;
    private Integer size = 20;
    private String sort = "startsAtUtc,asc";
}
