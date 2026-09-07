package com.backend1.backend1.model;

import lombok.Getter;

@Getter
public enum RoomType {
    SINGLE("Enkelrum"),
    DOUBLE("Dubbelrum");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }
}
