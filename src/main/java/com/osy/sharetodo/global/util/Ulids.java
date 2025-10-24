package com.osy.sharetodo.global.util;

import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.stereotype.Component;

@Component
public class Ulids {
    public String newUlid() {
        return UlidCreator.getUlid().toString();
    }
}
