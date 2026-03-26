/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum QuicApplicationErrorCode {
    NoError(0),
    RateLimited(1),
    AuthFailed(2),
    InvalidVersion(3),
    Timeout(4),
    ClientOutdated(5),
    ServerOutdated(6);

    public static final QuicApplicationErrorCode[] VALUES;
    private final int value;

    private QuicApplicationErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static QuicApplicationErrorCode fromValue(int value) {
        if (value >= 0 && value < VALUES.length) {
            return VALUES[value];
        }
        throw ProtocolException.invalidEnumValue("QuicApplicationErrorCode", value);
    }

    static {
        VALUES = QuicApplicationErrorCode.values();
    }
}

