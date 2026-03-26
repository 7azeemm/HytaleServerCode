/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum RotationFace {
    Up(0),
    Down(1),
    North(2),
    South(3),
    East(4),
    West(5),
    Camera(6);

    public static final RotationFace[] VALUES;
    private final int value;

    private RotationFace(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static RotationFace fromValue(int value) {
        if (value >= 0 && value < VALUES.length) {
            return VALUES[value];
        }
        throw ProtocolException.invalidEnumValue("RotationFace", value);
    }

    static {
        VALUES = RotationFace.values();
    }
}

