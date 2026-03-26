/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.spawning.blockstates;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import javax.annotation.Nullable;

public class SpawnMarkerBlock
implements Component<ChunkStore> {
    public static final BuilderCodec<SpawnMarkerBlock> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(SpawnMarkerBlock.class, SpawnMarkerBlock::new).append(new KeyedCodec<PersistentRef>("MarkerReference", PersistentRef.CODEC), (spawn, o) -> {
        spawn.spawnMarkerReference = o;
    }, spawn -> spawn.spawnMarkerReference).add()).append(new KeyedCodec<Data>("Config", Data.CODEC), (spawn, o) -> {
        spawn.config = o;
    }, spawn -> spawn.config).add()).build();
    private PersistentRef spawnMarkerReference;
    private float markerLostTimeout = 30.0f;
    @Nullable
    private Data config;

    public static ComponentType<ChunkStore, SpawnMarkerBlock> getComponentType() {
        return SpawningPlugin.get().getSpawnMarkerBlockComponentType();
    }

    public SpawnMarkerBlock() {
    }

    public SpawnMarkerBlock(PersistentRef spawnMarkerReference) {
        this.spawnMarkerReference = spawnMarkerReference;
    }

    public PersistentRef getSpawnMarkerReference() {
        return this.spawnMarkerReference;
    }

    @Nullable
    public Data getConfig() {
        return this.config;
    }

    public void setSpawnMarkerReference(PersistentRef spawnMarkerReference) {
        this.spawnMarkerReference = spawnMarkerReference;
    }

    public void refreshMarkerLostTimeout() {
        this.markerLostTimeout = 30.0f;
    }

    public boolean tickMarkerLostTimeout(float dt) {
        float f;
        this.markerLostTimeout -= dt;
        return f <= 0.0f;
    }

    @Override
    @Nullable
    public Component<ChunkStore> clone() {
        return new SpawnMarkerBlock(this.spawnMarkerReference != null ? new PersistentRef(this.spawnMarkerReference.getUuid()) : null);
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Data.class, Data::new).appendInherited(new KeyedCodec<String>("SpawnMarker", Codec.STRING), (spawn, s) -> {
            spawn.spawnMarker = s;
        }, spawn -> spawn.spawnMarker, (spawn, parent) -> {
            spawn.spawnMarker = parent.spawnMarker;
        }).documentation("The spawn marker to use.").addValidator(Validators.nonNull()).addValidatorLate(() -> SpawnMarker.VALIDATOR_CACHE.getValidator().late()).add()).appendInherited(new KeyedCodec<Vector3i>("MarkerOffset", Vector3i.CODEC), (spawn, o) -> {
            spawn.markerOffset = o;
        }, spawn -> spawn.markerOffset, (spawn, parent) -> {
            spawn.markerOffset = parent.markerOffset;
        }).documentation("An offset from the block at which the marker entity should be spawned.").add()).build();
        private String spawnMarker;
        private Vector3i markerOffset;

        protected Data() {
        }

        public String getSpawnMarker() {
            return this.spawnMarker;
        }

        public Vector3i getMarkerOffset() {
            return this.markerOffset;
        }
    }
}

