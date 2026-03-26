/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.protocol.packets.voice;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum VoiceCodec {
    Opus(0);

    public static final VoiceCodec[] VALUES;
    private final int value;

    private VoiceCodec(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static VoiceCodec fromValue(int value) {
        if (value >= 0 && value < VALUES.length) {
            return VALUES[value];
        }
        throw ProtocolException.invalidEnumValue("VoiceCodec", value);
    }

    static {
        VALUES = VoiceCodec.values();
    }
}

