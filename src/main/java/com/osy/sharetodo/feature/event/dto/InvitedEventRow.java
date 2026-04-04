package com.osy.sharetodo.feature.event.dto;

import com.osy.sharetodo.feature.event.domain.Event;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class InvitedEventRow {

    private final Event event;
    private final String invitationUid;

    @QueryProjection
    public InvitedEventRow(Event event, String invitationUid) {
        this.event = event;
        this.invitationUid = invitationUid;
    }
}