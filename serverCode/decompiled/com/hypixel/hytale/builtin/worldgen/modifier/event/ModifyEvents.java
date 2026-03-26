/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.worldgen.modifier.event;

import com.hypixel.hytale.builtin.worldgen.modifier.event.EventType;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.container.CoverContainer;
import com.hypixel.hytale.server.worldgen.container.EnvironmentContainer;
import com.hypixel.hytale.server.worldgen.container.LayerContainer;
import com.hypixel.hytale.server.worldgen.container.PrefabContainer;
import com.hypixel.hytale.server.worldgen.container.TintContainer;
import com.hypixel.hytale.server.worldgen.container.WaterContainer;
import com.hypixel.hytale.server.worldgen.loader.context.BiomeFileContext;
import com.hypixel.hytale.server.worldgen.loader.context.CaveFileContext;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import javax.annotation.Nonnull;

public interface ModifyEvents {

    public static final class CavePrefabs
    extends Record
    implements ModifyEvent<CavePrefabContainer.CavePrefabEntry> {
        @Nonnull
        private final CaveFileContext file;
        @Nonnull
        private final List<CavePrefabContainer.CavePrefabEntry> entries;
        @Nonnull
        private final ModifyEvent.ContentLoader<CavePrefabContainer.CavePrefabEntry> loader;

        public CavePrefabs(@Nonnull CaveFileContext file, @Nonnull List<CavePrefabContainer.CavePrefabEntry> entries, @Nonnull ModifyEvent.ContentLoader<CavePrefabContainer.CavePrefabEntry> loader) {
            this.file = file;
            this.entries = entries;
            this.loader = loader;
        }

        @Override
        @Nonnull
        public EventType type() {
            return EventType.Cave_Prefabs;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CavePrefabs.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CavePrefabs.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CavePrefabs.class, "file;entries;loader", "file", "entries", "loader"}, this, o);
        }

        @Nonnull
        public CaveFileContext file() {
            return this.file;
        }

        @Override
        @Nonnull
        public List<CavePrefabContainer.CavePrefabEntry> entries() {
            return this.entries;
        }

        @Override
        @Nonnull
        public ModifyEvent.ContentLoader<CavePrefabContainer.CavePrefabEntry> loader() {
            return this.loader;
        }
    }

    public static final class CaveCovers
    extends Record
    implements ModifyEvent<CaveNodeType.CaveNodeCoverEntry> {
        @Nonnull
        private final CaveFileContext file;
        @Nonnull
        private final List<CaveNodeType.CaveNodeCoverEntry> entries;
        @Nonnull
        private final ModifyEvent.ContentLoader<CaveNodeType.CaveNodeCoverEntry> loader;

        public CaveCovers(@Nonnull CaveFileContext file, @Nonnull List<CaveNodeType.CaveNodeCoverEntry> entries, @Nonnull ModifyEvent.ContentLoader<CaveNodeType.CaveNodeCoverEntry> loader) {
            this.file = file;
            this.entries = entries;
            this.loader = loader;
        }

        @Override
        @Nonnull
        public EventType type() {
            return EventType.Cave_Covers;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CaveCovers.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CaveCovers.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CaveCovers.class, "file;entries;loader", "file", "entries", "loader"}, this, o);
        }

        @Nonnull
        public CaveFileContext file() {
            return this.file;
        }

        @Override
        @Nonnull
        public List<CaveNodeType.CaveNodeCoverEntry> entries() {
            return this.entries;
        }

        @Override
        @Nonnull
        public ModifyEvent.ContentLoader<CaveNodeType.CaveNodeCoverEntry> loader() {
            return this.loader;
        }
    }

    public static final class CaveTypes
    extends Record
    implements ModifyEvent<CaveType> {
        @Nonnull
        private final CaveFileContext file;
        @Nonnull
        private final List<CaveType> entries;
        @Nonnull
        private final ModifyEvent.ContentLoader<CaveType> loader;

        public CaveTypes(@Nonnull CaveFileContext file, @Nonnull List<CaveType> entries, @Nonnull ModifyEvent.ContentLoader<CaveType> loader) {
            this.file = file;
            this.entries = entries;
            this.loader = loader;
        }

        @Override
        @Nonnull
        public EventType type() {
            return EventType.Cave_Types;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CaveTypes.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CaveTypes.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CaveTypes.class, "file;entries;loader", "file", "entries", "loader"}, this, o);
        }

        @Nonnull
        public CaveFileContext file() {
            return this.file;
        }

        @Override
        @Nonnull
        public List<CaveType> entries() {
            return this.entries;
        }

        @Override
        @Nonnull
        public ModifyEvent.ContentLoader<CaveType> loader() {
            return this.loader;
        }
    }

    public static final class BiomeTints
    extends Record
    implements ModifyEvent<TintContainer.TintContainerEntry> {
        @Nonnull
        private final BiomeFileContext file;
        @Nonnull
        private final List<TintContainer.TintContainerEntry> entries;
        @Nonnull
        private final ModifyEvent.ContentLoader<TintContainer.TintContainerEntry> loader;

        public BiomeTints(@Nonnull BiomeFileContext file, @Nonnull List<TintContainer.TintContainerEntry> entries, @Nonnull ModifyEvent.ContentLoader<TintContainer.TintContainerEntry> loader) {
            this.file = file;
            this.entries = entries;
            this.loader = loader;
        }

        @Override
        @Nonnull
        public EventType type() {
            return EventType.Biome_Tints;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BiomeTints.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BiomeTints.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BiomeTints.class, "file;entries;loader", "file", "entries", "loader"}, this, o);
        }

        @Nonnull
        public BiomeFileContext file() {
            return this.file;
        }

        @Override
        @Nonnull
        public List<TintContainer.TintContainerEntry> entries() {
            return this.entries;
        }

        @Override
        @Nonnull
        public ModifyEvent.ContentLoader<TintContainer.TintContainerEntry> loader() {
            return this.loader;
        }
    }

    public static final class BiomePrefabs
    extends Record
    implements ModifyEvent<PrefabContainer.PrefabContainerEntry> {
        @Nonnull
        private final BiomeFileContext file;
        @Nonnull
        private final List<PrefabContainer.PrefabContainerEntry> entries;
        @Nonnull
        private final ModifyEvent.ContentLoader<PrefabContainer.PrefabContainerEntry> loader;

        public BiomePrefabs(@Nonnull BiomeFileContext file, @Nonnull List<PrefabContainer.PrefabContainerEntry> entries, @Nonnull ModifyEvent.ContentLoader<PrefabContainer.PrefabContainerEntry> loader) {
            this.file = file;
            this.entries = entries;
            this.loader = loader;
        }

        @Override
        @Nonnull
        public EventType type() {
            return EventType.Biome_Prefabs;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BiomePrefabs.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BiomePrefabs.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BiomePrefabs.class, "file;entries;loader", "file", "entries", "loader"}, this, o);
        }

        @Nonnull
        public BiomeFileContext file() {
            return this.file;
        }

        @Override
        @Nonnull
        public List<PrefabContainer.PrefabContainerEntry> entries() {
            return this.entries;
        }

        @Override
        @Nonnull
        public ModifyEvent.ContentLoader<PrefabContainer.PrefabContainerEntry> loader() {
            return this.loader;
        }
    }

    public static final class BiomeStaticLayers
    extends Record
    implements ModifyEvent<LayerContainer.StaticLayer> {
        @Nonnull
        private final BiomeFileContext file;
        @Nonnull
        private final List<LayerContainer.StaticLayer> entries;
        @Nonnull
        private final ModifyEvent.ContentLoader<LayerContainer.StaticLayer> loader;

        public BiomeStaticLayers(@Nonnull BiomeFileContext file, @Nonnull List<LayerContainer.StaticLayer> entries, @Nonnull ModifyEvent.ContentLoader<LayerContainer.StaticLayer> loader) {
            this.file = file;
            this.entries = entries;
            this.loader = loader;
        }

        @Override
        @Nonnull
        public EventType type() {
            return EventType.Biome_Static_Layers;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BiomeStaticLayers.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BiomeStaticLayers.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BiomeStaticLayers.class, "file;entries;loader", "file", "entries", "loader"}, this, o);
        }

        @Nonnull
        public BiomeFileContext file() {
            return this.file;
        }

        @Override
        @Nonnull
        public List<LayerContainer.StaticLayer> entries() {
            return this.entries;
        }

        @Override
        @Nonnull
        public ModifyEvent.ContentLoader<LayerContainer.StaticLayer> loader() {
            return this.loader;
        }
    }

    public static final class BiomeDynamicLayers
    extends Record
    implements ModifyEvent<LayerContainer.DynamicLayer> {
        @Nonnull
        private final BiomeFileContext file;
        @Nonnull
        private final List<LayerContainer.DynamicLayer> entries;
        @Nonnull
        private final ModifyEvent.ContentLoader<LayerContainer.DynamicLayer> loader;

        public BiomeDynamicLayers(@Nonnull BiomeFileContext file, @Nonnull List<LayerContainer.DynamicLayer> entries, @Nonnull ModifyEvent.ContentLoader<LayerContainer.DynamicLayer> loader) {
            this.file = file;
            this.entries = entries;
            this.loader = loader;
        }

        @Override
        @Nonnull
        public EventType type() {
            return EventType.Biome_Dynamic_Layers;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BiomeDynamicLayers.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BiomeDynamicLayers.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BiomeDynamicLayers.class, "file;entries;loader", "file", "entries", "loader"}, this, o);
        }

        @Nonnull
        public BiomeFileContext file() {
            return this.file;
        }

        @Override
        @Nonnull
        public List<LayerContainer.DynamicLayer> entries() {
            return this.entries;
        }

        @Override
        @Nonnull
        public ModifyEvent.ContentLoader<LayerContainer.DynamicLayer> loader() {
            return this.loader;
        }
    }

    public static final class BiomeFluids
    extends Record
    implements ModifyEvent<WaterContainer.Entry> {
        @Nonnull
        private final BiomeFileContext file;
        @Nonnull
        private final List<WaterContainer.Entry> entries;
        @Nonnull
        private final ModifyEvent.ContentLoader<WaterContainer.Entry> loader;

        public BiomeFluids(@Nonnull BiomeFileContext file, @Nonnull List<WaterContainer.Entry> entries, @Nonnull ModifyEvent.ContentLoader<WaterContainer.Entry> loader) {
            this.file = file;
            this.entries = entries;
            this.loader = loader;
        }

        @Override
        @Nonnull
        public EventType type() {
            return EventType.Biome_Fluids;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BiomeFluids.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BiomeFluids.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BiomeFluids.class, "file;entries;loader", "file", "entries", "loader"}, this, o);
        }

        @Nonnull
        public BiomeFileContext file() {
            return this.file;
        }

        @Override
        @Nonnull
        public List<WaterContainer.Entry> entries() {
            return this.entries;
        }

        @Override
        @Nonnull
        public ModifyEvent.ContentLoader<WaterContainer.Entry> loader() {
            return this.loader;
        }
    }

    public static final class BiomeEnvironments
    extends Record
    implements ModifyEvent<EnvironmentContainer.EnvironmentContainerEntry> {
        @Nonnull
        private final BiomeFileContext file;
        @Nonnull
        private final List<EnvironmentContainer.EnvironmentContainerEntry> entries;
        @Nonnull
        private final ModifyEvent.ContentLoader<EnvironmentContainer.EnvironmentContainerEntry> loader;

        public BiomeEnvironments(@Nonnull BiomeFileContext file, @Nonnull List<EnvironmentContainer.EnvironmentContainerEntry> entries, @Nonnull ModifyEvent.ContentLoader<EnvironmentContainer.EnvironmentContainerEntry> loader) {
            this.file = file;
            this.entries = entries;
            this.loader = loader;
        }

        @Override
        @Nonnull
        public EventType type() {
            return EventType.Biome_Environments;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BiomeEnvironments.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BiomeEnvironments.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BiomeEnvironments.class, "file;entries;loader", "file", "entries", "loader"}, this, o);
        }

        @Nonnull
        public BiomeFileContext file() {
            return this.file;
        }

        @Override
        @Nonnull
        public List<EnvironmentContainer.EnvironmentContainerEntry> entries() {
            return this.entries;
        }

        @Override
        @Nonnull
        public ModifyEvent.ContentLoader<EnvironmentContainer.EnvironmentContainerEntry> loader() {
            return this.loader;
        }
    }

    public static final class BiomeCovers
    extends Record
    implements ModifyEvent<CoverContainer.CoverContainerEntry> {
        @Nonnull
        private final BiomeFileContext file;
        @Nonnull
        private final List<CoverContainer.CoverContainerEntry> entries;
        @Nonnull
        private final ModifyEvent.ContentLoader<CoverContainer.CoverContainerEntry> loader;

        public BiomeCovers(@Nonnull BiomeFileContext file, @Nonnull List<CoverContainer.CoverContainerEntry> entries, @Nonnull ModifyEvent.ContentLoader<CoverContainer.CoverContainerEntry> loader) {
            this.file = file;
            this.entries = entries;
            this.loader = loader;
        }

        @Override
        @Nonnull
        public EventType type() {
            return EventType.Biome_Covers;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BiomeCovers.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BiomeCovers.class, "file;entries;loader", "file", "entries", "loader"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BiomeCovers.class, "file;entries;loader", "file", "entries", "loader"}, this, o);
        }

        @Nonnull
        public BiomeFileContext file() {
            return this.file;
        }

        @Override
        @Nonnull
        public List<CoverContainer.CoverContainerEntry> entries() {
            return this.entries;
        }

        @Override
        @Nonnull
        public ModifyEvent.ContentLoader<CoverContainer.CoverContainerEntry> loader() {
            return this.loader;
        }
    }
}

