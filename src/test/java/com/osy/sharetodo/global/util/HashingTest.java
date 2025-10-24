package com.osy.sharetodo.global.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HashingTest {

    @Test
    void sha256_32bytes_and_stable() {
        Hashing hashing = new Hashing();
        hashing.setPepper("pepper");
        byte[] a = hashing.sha256("test@example.com");
        byte[] b = hashing.sha256("test@example.com");
        assertThat(a).hasSize(32);
        assertThat(a).isEqualTo(b);
    }
}