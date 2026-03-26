/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.protocol.packets.stream;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum StreamType {
    Game(0),
    Voice(1);

    public static final StreamType[] VALUES;
    private final int value;

    private StreamType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static StreamType fromValue(int value) {
        if (value >= 0 && value < VALUES.length) {
            return VALUES[value];
        }
        throw ProtocolException.invalidEnumValue("StreamType", value);
    }

    static {
        VALUES = StreamType.values();
    }
}

