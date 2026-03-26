/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.worldgen.loader.container;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.json.DoubleRangeJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoiseMaskConditionJsonLoader;
import com.hypixel.hytale.procedurallib.json.NoisePropertyJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.procedurallib.supplier.DoubleRangeNoiseSupplier;
import com.hypixel.hytale.procedurallib.supplier.IDoubleCoordinateSupplier;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.WaterContainer;
import com.hypixel.hytale.server.worldgen.loader.context.BiomeFileContext;
import com.hypixel.hytale.server.worldgen.util.ConstantNoiseProperty;
import com.hypixel.hytale.server.worldgen.util.ListPool;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WaterContainerJsonLoader
extends JsonLoader<SeedStringResource, WaterContainer> {
    @Nonnull
    private final BiomeFileContext biomeContext;

    public WaterContainerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, @Nonnull BiomeFileContext biomeContext) {
        super(seed.append(".WaterContainer"), dataFolder, json);
        this.biomeContext = biomeContext;
    }

    @Override
    @Nonnull
    public WaterContainer load() {
        try (ListPool.Resource<WaterContainer.Entry> entries = WaterContainer.ENTRY_POOL.acquire();){
            if (this.has("Block")) {
                String blockString;
                String blockTypeKey = blockString = this.get("Block").getAsString();
                int index = BlockType.getAssetMap().getIndex(blockTypeKey);
                if (index == Integer.MIN_VALUE) {
                    throw new Error(String.format("Could not find Fluid for fluid: %s", blockTypeKey.toString()));
                }
                IDoubleRange array = new DoubleRangeJsonLoader(this.seed, this.dataFolder, this.get("Height"), 0.0).load();
                NoiseProperty heightmapNoise = ConstantNoiseProperty.DEFAULT_ZERO;
                if (this.has("Heightmap")) {
                    heightmapNoise = new NoisePropertyJsonLoader(this.seed, this.dataFolder, this.get("Heightmap")).load();
                }
                DoubleRangeNoiseSupplier height = new DoubleRangeNoiseSupplier(array, heightmapNoise);
                entries.add(new WaterContainer.Entry(index, 0, new DoubleRangeNoiseSupplier(DoubleRange.ZERO, ConstantNoiseProperty.DEFAULT_ZERO), height, DefaultCoordinateCondition.DEFAULT_TRUE));
            } else if (this.has("Fluid")) {
                String fluidString = this.get("Fluid").getAsString();
                int index = Fluid.getAssetMap().getIndex(fluidString);
                if (index == Integer.MIN_VALUE) {
                    throw new Error(String.format("Could not find Fluid for fluid: %s", fluidString));
                }
                IDoubleRange array = new DoubleRangeJsonLoader(this.seed, this.dataFolder, this.get("Height"), 0.0).load();
                NoiseProperty heightmapNoise = ConstantNoiseProperty.DEFAULT_ZERO;
                if (this.has("Heightmap")) {
                    heightmapNoise = new NoisePropertyJsonLoader(this.seed, this.dataFolder, this.get("Heightmap")).load();
                }
                DoubleRangeNoiseSupplier height = new DoubleRangeNoiseSupplier(array, heightmapNoise);
                entries.add(new WaterContainer.Entry(0, index, new DoubleRangeNoiseSupplier(DoubleRange.ZERO, ConstantNoiseProperty.DEFAULT_ZERO), height, DefaultCoordinateCondition.DEFAULT_TRUE));
            } else {
                JsonArray fluidArray = this.mustGetArray("Entries", EMPTY_ARRAY);
                for (int i = 0; i < fluidArray.size(); ++i) {
                    try {
                        entries.add(new WaterContainerEntryJsonLoader(this.seed.append(String.format("-%s", i)), this.dataFolder, fluidArray.get(i)).load());
                        continue;
                    }
                    catch (Throwable e) {
                        throw new Error(String.format("Failed to load TintContainerEntry #%s", i), e);
                    }
                }
            }
            ModifyEvent.SeedGenerator seed = new ModifyEvent.SeedGenerator(this.seed);
            ModifyEvent.dispatch(ModifyEvents.BiomeFluids.class, new ModifyEvents.BiomeFluids(this.biomeContext, entries, content -> new WaterContainerEntryJsonLoader(seed.next(), this.dataFolder, this.getOrLoad(content)).load()));
            WaterContainer waterContainer = new WaterContainer(entries.toArray());
            return waterContainer;
        }
    }

    public static interface Constants {
        public static final String KEY_ENTRIES = "Entries";
        public static final String KEY_ENTRY_BLOCK = "Block";
        public static final String ERROR_ENTRY_NO_BLOCK = "Could not find block information. Keyword: Block";
        public static final String ERROR_ENTRY_FLUID_BLOCK = "Could not find BlockType for block: %s";
        public static final String KEY_ENTRY_FLUID = "Fluid";
        public static final String ERROR_ENTRY_NO_FLUID = "Could not find fluid information. Keyword: Fluid";
        public static final String ERROR_ENTRY_FLUID_TYPE = "Could not find Fluid for fluid: %s";
        public static final String KEY_ENTRY_MIN = "Min";
        public static final String KEY_ENTRY_MIN_NOISE = "MinNoise";
        public static final String KEY_ENTRY_MAX = "Max";
        public static final String KEY_ENTRY_MAX_NOISE = "MaxNoise";
        public static final String KEY_ENTRY_NOISE_MASK = "NoiseMask";
        public static final String ERROR_ENTRY_NO_MAX = "Could not find maximum of water container entry.";
    }

    private static class WaterContainerEntryJsonLoader
    extends JsonLoader<SeedStringResource, WaterContainer.Entry> {
        public WaterContainerEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
            super(seed.append(".Entry"), dataFolder, json);
        }

        @Override
        @Nonnull
        public WaterContainer.Entry load() {
            try {
                if (this.has("Fluid")) {
                    String fluidString = this.get("Fluid").getAsString();
                    int index = Fluid.getAssetMap().getIndex(fluidString);
                    if (index == Integer.MIN_VALUE) {
                        throw new Error(String.format("Could not find Fluid for fluid: %s", fluidString));
                    }
                    return new WaterContainer.Entry(0, index, this.loadMin(), this.loadMax(), this.loadNoiseMask());
                }
                if (this.has("Block")) {
                    String blockString;
                    String blockTypeKey = blockString = this.get("Block").getAsString();
                    int index = BlockType.getAssetMap().getIndex(blockTypeKey);
                    if (index == Integer.MIN_VALUE) {
                        throw new Error(String.format("Could not find Fluid for fluid: %s", blockTypeKey.toString()));
                    }
                    return new WaterContainer.Entry(index, 0, this.loadMin(), this.loadMax(), this.loadNoiseMask());
                }
                throw new IllegalArgumentException("Could not find fluid information. Keyword: Fluid");
            }
            catch (Error e) {
                throw new Error("Failed to load water container.", e);
            }
        }

        @Nonnull
        private IDoubleCoordinateSupplier loadMin() {
            IDoubleRange array = new DoubleRangeJsonLoader(this.seed, this.dataFolder, this.get("Min"), 0.0).load();
            NoiseProperty minNoise = this.loadNoise("MinNoise");
            return new DoubleRangeNoiseSupplier(array, minNoise);
        }

        @Nonnull
        private IDoubleCoordinateSupplier loadMax() {
            if (!this.has("Max")) {
                throw new IllegalArgumentException("Could not find maximum of water container entry.");
            }
            IDoubleRange array = new DoubleRangeJsonLoader(this.seed, this.dataFolder, this.get("Max"), 0.0).load();
            NoiseProperty maxNoise = this.loadNoise("MaxNoise");
            return new DoubleRangeNoiseSupplier(array, maxNoise);
        }

        @Nullable
        private NoiseProperty loadNoise(String key) {
            NoiseProperty maxNoise = ConstantNoiseProperty.DEFAULT_ZERO;
            if (this.has(key)) {
                maxNoise = new NoisePropertyJsonLoader(this.seed, this.dataFolder, this.get(key)).load();
            }
            return maxNoise;
        }

        @Nonnull
        private ICoordinateCondition loadNoiseMask() {
            ICoordinateCondition mask = DefaultCoordinateCondition.DEFAULT_TRUE;
            if (this.has("NoiseMask")) {
                mask = new NoiseMaskConditionJsonLoader(this.seed, this.dataFolder, this.get("NoiseMask")).load();
            }
            return mask;
        }
    }
}

