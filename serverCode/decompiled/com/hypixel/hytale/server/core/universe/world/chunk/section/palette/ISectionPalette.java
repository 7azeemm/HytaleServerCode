/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.function.consumer.BiIntConsumer;
import com.hypixel.hytale.protocol.packets.world.PaletteType;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.ByteSectionPalette;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.EmptySectionPalette;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.HalfByteSectionPalette;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.ShortSectionPalette;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;

public interface ISectionPalette {
    public PaletteType getPaletteType();

    public SetResult set(int var1, int var2);

    public int get(int var1);

    public boolean contains(int var1);

    public boolean containsAny(IntList var1);

    default public boolean isSolid(int id) {
        return this.count() == 1 && this.contains(id);
    }

    public int count();

    public int count(int var1);

    public IntSet values();

    public void forEachValue(IntConsumer var1);

    public Int2ShortMap valueCounts();

    public void find(@Nonnull IntList var1, @Nonnull IntConsumer var2);

    public void find(@Nonnull IntList var1, @Nonnull BiIntConsumer var2);

    @Deprecated(since="2026-02-26", forRemoval=true)
    default public void find(@Nonnull IntList ids, @Nonnull IntSet ignoredInternalIdHolder, @Nonnull IntConsumer indexConsumer) {
        this.find(ids, indexConsumer);
    }

    public boolean shouldDemote();

    public ISectionPalette demote();

    public ISectionPalette promote();

    public void serializeForPacket(ByteBuf var1);

    public void serialize(KeySerializer var1, ByteBuf var2);

    public void deserialize(ToIntFunction<ByteBuf> var1, ByteBuf var2, int var3);

    @Nonnull
    public static ISectionPalette from(@Nonnull int[] data, @Nonnull Int2ShortMap idCounts) {
        if (idCounts.size() == 1 && idCounts.containsKey(0)) {
            return EmptySectionPalette.INSTANCE;
        }
        if (idCounts.size() < 16) {
            return new HalfByteSectionPalette(data, idCounts);
        }
        if (idCounts.size() < 256) {
            return new ByteSectionPalette(data, idCounts);
        }
        if (idCounts.size() < 65536) {
            return new ShortSectionPalette(data, idCounts);
        }
        throw new UnsupportedOperationException("Too many block types for palette.");
    }

    @FunctionalInterface
    public static interface KeySerializer {
        public void serialize(ByteBuf var1, int var2);
    }

    public static enum SetResult {
        ADDED_OR_REMOVED,
        CHANGED,
        UNCHANGED,
        REQUIRES_PROMOTE;

    }
}

