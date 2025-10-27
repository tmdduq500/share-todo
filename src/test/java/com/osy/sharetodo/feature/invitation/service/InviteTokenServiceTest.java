package com.osy.sharetodo.feature.invitation.service;

import com.osy.sharetodo.global.util.Hashing;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class InviteTokenServiceTest {
    @Test
    void token_and_hash() {
        InviteTokenService s = new InviteTokenService(new Hashing());
        String t = s.newToken();
        assertThat(t).isNotBlank();
        assertThat(t.length()).isBetween(40, 50); // base64url(32B) ~= 43
        byte[] h = s.hash(t);
        assertThat(h).hasSize(32);
    }
}