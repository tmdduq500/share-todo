package com.osy.sharetodo.global.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UlidsTest {
    @Test
    void newUlid_26chars_unique() {
        Ulids ulids = new Ulids();
        String u1 = ulids.newUlid();
        String u2 = ulids.newUlid();
        assertThat(u1).hasSize(26);
        assertThat(u1).isNotEqualTo(u2);
    }
}