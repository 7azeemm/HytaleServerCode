/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.worldgen.modifier.op;

import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import javax.annotation.Nonnull;

public interface Op {
    public static final Op[] EMPTY_ARRAY = new Op[0];
    public static final String TYPE_KEY = "Operation";
    public static final CodecMapCodec<Op> TYPE_CODEC = new CodecMapCodec("Operation", true);
    public static final ArrayCodec<Op> ARRAY_CODEC = new ArrayCodec<Op>(TYPE_CODEC, Op[]::new);

    public <T> void apply(@Nonnull ModifyEvent<T> var1) throws Error;
}

