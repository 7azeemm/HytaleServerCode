/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.worldgen.modifier.content;

import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import javax.annotation.Nonnull;

public interface Content {
    public static final String TYPE_KEY = "Type";
    public static final Content[] EMPTY_ARRAY = new Content[0];
    public static final CodecMapCodec<Content> TYPE_CODEC = new CodecMapCodec("Type", true);
    public static final ArrayCodec<Content> ARRAY_CODEC = new ArrayCodec<Content>(TYPE_CODEC, Content[]::new);

    @Nonnull
    public JsonElement get();
}

