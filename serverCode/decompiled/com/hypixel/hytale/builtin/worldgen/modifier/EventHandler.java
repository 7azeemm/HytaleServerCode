/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.worldgen.modifier;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.builtin.worldgen.modifier.Target;
import com.hypixel.hytale.builtin.worldgen.modifier.WorldGenModifier;
import com.hypixel.hytale.builtin.worldgen.modifier.event.EventType;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.op.Op;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.worldgen.util.ListPool;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public final class EventHandler
implements AutoCloseable {
    private static final EventHandler EMPTY = new EventHandler();
    private static final ThreadLocal<EventHandler> SCOPED_HANDLER = ThreadLocal.withInitial(() -> EMPTY);
    private static final ListPool<Modifier> POOL = new ListPool<Modifier>(5, Modifier.EMPTY_ARRAY);
    private static final ListPool<PriorityEntry> ENTRY_POOL = new ListPool<PriorityEntry>(5, PriorityEntry.EMPTY_ARRAY);
    @Nonnull
    private final EnumMap<EventType, Modifier[]> events = new EnumMap(EventType.class);

    private EventHandler() {
    }

    private EventHandler(@Nonnull String root) {
        try (ListPool.Resource<PriorityEntry> entries = ENTRY_POOL.acquire();){
            List<AssetPack> packs = AssetModule.get().getAssetPacks();
            Object2IntOpenHashMap<String> packPriorities = new Object2IntOpenHashMap<String>();
            for (int i = 0; i < packs.size(); ++i) {
                packPriorities.put(packs.get(i).getName(), i);
            }
            for (Map.Entry<String, WorldGenModifier> entry : WorldGenModifier.ASSET_MAP.getAssetMap().entrySet()) {
                if (!entry.getValue().getTarget().matchesRoot(root)) continue;
                String pack = WorldGenModifier.ASSET_MAP.getAssetPack(entry.getKey());
                int priority = packPriorities.getOrDefault((Object)pack, 0);
                entries.add(new PriorityEntry(entry.getValue(), priority));
            }
            Collections.sort(entries);
            for (EventType type : EventType.VALUES) {
                try (ListPool.Resource<Modifier> modifiers = POOL.acquire();){
                    for (int i = 0; i < entries.size(); ++i) {
                        PriorityEntry entry = (PriorityEntry)entries.get(i);
                        Op[] ops = entry.modifier.getOperations(type);
                        if (ops.length == 0) continue;
                        modifiers.add(new Modifier(entry.modifier.target, ops));
                    }
                    this.events.put(type, modifiers.toArray());
                }
            }
        }
    }

    @Nonnull
    public Modifier[] get(@Nonnull EventType type) {
        return this.events.getOrDefault((Object)type, Modifier.EMPTY_ARRAY);
    }

    @Override
    public void close() {
        this.events.clear();
        SCOPED_HANDLER.set(EMPTY);
    }

    public static <T> void handle(@Nonnull ModifyEvent<T> event) {
        EventHandler handler = SCOPED_HANDLER.get();
        String contentPath = event.file().getContentPath();
        for (Modifier modifier : handler.get(event.type())) {
            if (!modifier.target().matchesRule(contentPath)) continue;
            for (Op op : modifier.ops()) {
                op.apply(event);
            }
        }
    }

    public static EventHandler acquire(@Nonnull Path root) {
        assert (SCOPED_HANDLER.get() == EMPTY) : "EventHandler already open or was not closed!";
        EventHandler handler = new EventHandler(root.getFileName().toString());
        SCOPED_HANDLER.set(handler);
        return handler;
    }

    public record PriorityEntry(WorldGenModifier modifier, int packPriority) implements Comparable<PriorityEntry>
    {
        public static final PriorityEntry[] EMPTY_ARRAY = new PriorityEntry[0];

        @Override
        public int compareTo(PriorityEntry o) {
            if (this.modifier.priority == o.modifier.priority) {
                return Integer.compare(this.packPriority, o.packPriority);
            }
            return Integer.compare(this.modifier.priority.getValue(), o.modifier.priority.getValue());
        }
    }

    public record Modifier(@Nonnull Target target, @Nonnull Op[] ops) {
        public static final Modifier[] EMPTY_ARRAY = new Modifier[0];
    }
}

