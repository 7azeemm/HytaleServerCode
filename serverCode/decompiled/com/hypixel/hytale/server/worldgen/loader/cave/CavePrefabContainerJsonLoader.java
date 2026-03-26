/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.cave.CavePrefabEntryJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.CaveFileContext;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.util.ListPool;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class CavePrefabContainerJsonLoader
extends JsonLoader<SeedStringResource, CavePrefabContainer> {
    protected final CaveFileContext context;

    public CavePrefabContainerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, CaveFileContext context) {
        super(seed.append(".CavePrefabContainer"), dataFolder, json);
        this.context = context;
    }

    @Override
    @Nonnull
    public CavePrefabContainer load() {
        return new CavePrefabContainer(this.loadEntries());
    }

    @Nonnull
    protected CavePrefabContainer.CavePrefabEntry[] loadEntries() {
        try (ListPool.Resource<CavePrefabContainer.CavePrefabEntry> entries = CavePrefabContainer.ENTRY_POOL.acquire();){
            if (this.json != null) {
                JsonArray prefabArray = this.mustGetArray("Entries", null);
                for (int i = 0; i < prefabArray.size(); ++i) {
                    entries.add(new CavePrefabEntryJsonLoader(this.seed.append(String.format("-%s", i)), this.dataFolder, prefabArray.get(i), (ZoneFileContext)this.context.getParentContext()).load());
                }
            }
            ModifyEvent.SeedGenerator seed = new ModifyEvent.SeedGenerator(this.seed);
            ModifyEvent.dispatch(ModifyEvents.CavePrefabs.class, new ModifyEvents.CavePrefabs(this.context, entries, content -> new CavePrefabEntryJsonLoader(seed.next(), this.dataFolder, this.getOrLoad(content), (ZoneFileContext)this.context.getParentContext()).load()));
            CavePrefabContainer.CavePrefabEntry[] cavePrefabEntryArray = entries.toArray();
            return cavePrefabEntryArray;
        }
    }

    public static interface Constants {
        public static final String KEY_ENTRIES = "Entries";
        public static final String SEED_ENTRY_SUFFIX = "-%s";
        public static final String ERROR_NO_ENTRIES = "Could not find entries in prefab container. Keyword: Entries";
    }
}

