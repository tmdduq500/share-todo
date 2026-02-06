package com.osy.sharetodo.feature.event.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventCalendarRes extends EventListRes {

    // 내 일정/초대 일정 구분
    private Source source;

    public enum Source {
        OWN, INVITED
    }
}
