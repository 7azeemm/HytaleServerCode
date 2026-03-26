/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.worldgen.modifier.event;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.worldgen.modifier.event.EventType;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.context.FileContext;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ModifyEvent<T>
extends IEvent<EventType> {
    @Nonnull
    public EventType type();

    @Nonnull
    public FileContext<?> file();

    @Nonnull
    public List<T> entries();

    @Nonnull
    public ContentLoader<T> loader();

    public static <E extends ModifyEvent<?>> void dispatch(@Nonnull Class<E> type, @Nonnull E event) throws Error {
        try {
            HytaleServer.get().getEventBus().dispatchFor(type, event.type()).dispatch(event);
        }
        catch (Throwable error) {
            throw new Error(String.format("Failed to invoke ModifyEvent %s for file %s", new Object[]{event.type(), event.file().getContentPath()}), error);
        }
    }

    public static class SeedGenerator<K extends SeedStringResource> {
        private final SeedString<K> seed;
        private int id = 0;

        public SeedGenerator(@Nonnull SeedString<K> seed) {
            this.seed = seed;
        }

        public SeedString<K> next() {
            return this.seed.append("-modified-" + this.id++);
        }
    }

    @FunctionalInterface
    public static interface ContentLoader<T> {
        @Nullable
        public T load(@Nonnull JsonElement var1) throws Exception;
    }
}

