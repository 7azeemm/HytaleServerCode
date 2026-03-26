/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.protocol.ItemHudUIType;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class ItemHudUI
implements NetworkSerializable<com.hypixel.hytale.protocol.ItemHudUI> {
    public static final BuilderCodec<ItemHudUI> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ItemHudUI.class, ItemHudUI::new).addField(new KeyedCodec<String>("Path", Codec.STRING), (entry, s) -> {
        entry.path = s;
    }, entry -> entry.path)).addField(new KeyedCodec<ItemHudUIType>("Type", new EnumCodec<ItemHudUIType>(ItemHudUIType.class)), (entry, t) -> {
        entry.type = t;
    }, entry -> entry.type)).build();
    protected String path;
    protected ItemHudUIType type = ItemHudUIType.Hud;

    public String getPath() {
        return this.path;
    }

    public ItemHudUIType getType() {
        return this.type;
    }

    @Override
    @Nonnull
    public com.hypixel.hytale.protocol.ItemHudUI toPacket() {
        com.hypixel.hytale.protocol.ItemHudUI packet = new com.hypixel.hytale.protocol.ItemHudUI();
        packet.path = this.path;
        packet.type = this.type;
        return packet;
    }

    @Nonnull
    public String toString() {
        return "ItemHudUI{path='" + this.path + "', type=" + String.valueOf((Object)this.type) + "}";
    }
}

