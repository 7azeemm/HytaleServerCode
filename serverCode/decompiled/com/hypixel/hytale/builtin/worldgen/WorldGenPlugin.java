/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.worldgen;

import com.hypixel.hytale.builtin.worldgen.modifier.EventHandler;
import com.hypixel.hytale.builtin.worldgen.modifier.WorldGenModifier;
import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.content.FileRef;
import com.hypixel.hytale.builtin.worldgen.modifier.event.EventType;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.builtin.worldgen.modifier.op.AddOp;
import com.hypixel.hytale.builtin.worldgen.modifier.op.Op;
import com.hypixel.hytale.builtin.worldgen.modifier.op.RemoveOp;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.worldgen.BiomeDataSystem;
import com.hypixel.hytale.server.worldgen.HytaleWorldGenProvider;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldGenPlugin
extends JavaPlugin {
    private static final String VERSIONS_DIR_NAME = "$Versions";
    private static final String MANIFEST_FILENAME = "manifest.json";
    private static WorldGenPlugin instance;

    public static WorldGenPlugin get() {
        return instance;
    }

    public WorldGenPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        instance = this;
        this.getEntityStoreRegistry().registerSystem(new BiomeDataSystem());
        IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(1), "Hytale", (Class<IWorldGenProvider>)HytaleWorldGenProvider.class, HytaleWorldGenProvider.CODEC);
        this.getEventRegistry().register(ModifyEvents.BiomeCovers.class, EventType.Biome_Covers, EventHandler::handle);
        this.getEventRegistry().register(ModifyEvents.BiomeEnvironments.class, EventType.Biome_Environments, EventHandler::handle);
        this.getEventRegistry().register(ModifyEvents.BiomeFluids.class, EventType.Biome_Fluids, EventHandler::handle);
        this.getEventRegistry().register(ModifyEvents.BiomeDynamicLayers.class, EventType.Biome_Dynamic_Layers, EventHandler::handle);
        this.getEventRegistry().register(ModifyEvents.BiomeStaticLayers.class, EventType.Biome_Static_Layers, EventHandler::handle);
        this.getEventRegistry().register(ModifyEvents.BiomePrefabs.class, EventType.Biome_Prefabs, EventHandler::handle);
        this.getEventRegistry().register(ModifyEvents.BiomeTints.class, EventType.Biome_Tints, EventHandler::handle);
        this.getEventRegistry().register(ModifyEvents.CaveTypes.class, EventType.Cave_Types, EventHandler::handle);
        this.getEventRegistry().register(ModifyEvents.CaveCovers.class, EventType.Cave_Covers, EventHandler::handle);
        this.getEventRegistry().register(ModifyEvents.CavePrefabs.class, EventType.Cave_Prefabs, EventHandler::handle);
        this.getCodecRegistry(Content.TYPE_CODEC).register(Priority.DEFAULT, "File", FileRef.class, FileRef.CODEC);
        this.getCodecRegistry(Op.TYPE_CODEC).register(Priority.DEFAULT, "Add", AddOp.class, AddOp.CODEC).register(Priority.NORMAL, "Remove", RemoveOp.class, RemoveOp.CODEC);
        this.getAssetRegistry().register(((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(WorldGenModifier.class, WorldGenModifier.ASSET_MAP).setCodec(WorldGenModifier.ASSET_CODEC)).setPath("WorldGen/Modifier")).setIdProvider(WorldGenModifier.class)).setKeyFunction(WorldGenModifier::getId)).setReplaceOnRemove(WorldGenModifier::new)).build());
        AssetModule assets = AssetModule.get();
        if (assets.getAssetPacks().isEmpty()) {
            this.getLogger().at(Level.SEVERE).log("No asset packs loaded");
            return;
        }
        FileIO.setDefaultRoot(assets.getBaseAssetPack().getRoot());
        List<Version> packs = WorldGenPlugin.loadVersionPacks(assets);
        Object2ObjectOpenHashMap<String, Semver> versions = new Object2ObjectOpenHashMap<String, Semver>();
        for (Version version : packs) {
            WorldGenPlugin.validateVersion(version, packs);
            assets.registerPack(version.getPackName(), version.path, version.manifest, false);
            Semver latest = versions.get(version.name);
            if (latest != null && version.manifest.getVersion().compareTo(latest) <= 0) continue;
            versions.put(version.name, version.manifest.getVersion());
        }
        HytaleWorldGenProvider.CODEC.setVersions(versions);
    }

    private static List<Version> loadVersionPacks(@Nonnull AssetModule assets) {
        ObjectArrayList<Version> objectArrayList;
        block10: {
            Path versionsDir = WorldGenPlugin.getVersionsPath();
            if (!Files.exists(versionsDir, new LinkOption[0])) {
                return ObjectLists.emptyList();
            }
            Path root = assets.getBaseAssetPack().getRoot();
            Path assetPath = root.relativize(Universe.getWorldGenPath());
            DirectoryStream<Path> stream = Files.newDirectoryStream(versionsDir);
            try {
                ObjectArrayList<Version> list = new ObjectArrayList<Version>();
                for (Path path : stream) {
                    PluginManifest manifest;
                    Path manifestPath;
                    String name;
                    if (!Files.isDirectory(path, new LinkOption[0]) || (name = WorldGenPlugin.getWorldConfigName(path, assetPath)) == null || !Files.exists(manifestPath = path.resolve(MANIFEST_FILENAME), new LinkOption[0]) || (manifest = WorldGenPlugin.loadManifest(manifestPath)) == null) continue;
                    list.add(new Version(name, path, manifest));
                }
                Collections.sort(list);
                objectArrayList = list;
                if (stream == null) break block10;
            }
            catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            stream.close();
        }
        return objectArrayList;
    }

    private static void validateVersion(@Nonnull Version version, @Nonnull List<Version> versions) {
        if (version.manifest.getVersion().compareTo(HytaleWorldGenProvider.MIN_VERSION) <= 0) {
            throw new IllegalArgumentException(String.format("Invalid $Version AssetPack: %s. Pack version number: %s must be greater than: %s", version.path(), version.manifest.getVersion(), HytaleWorldGenProvider.MIN_VERSION));
        }
        for (Version other : versions) {
            if (other == version || !version.name().equals(other.name()) || !version.manifest.getVersion().equals(other.manifest.getVersion())) continue;
            throw new IllegalArgumentException(String.format("$Version AssetPack: %s conflicts with pack: %s. Pack version numbers must be different. Found: %s in both", version.path(), other.path(), version.manifest.getVersion()));
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    private static String getWorldConfigName(@Nonnull Path packPath, @Nonnull Path assetPath) {
        Path filepath = packPath.resolve(assetPath);
        if (!Files.exists(filepath, new LinkOption[0]) || !Files.isDirectory(filepath, new LinkOption[0])) {
            LogUtil.getLogger().at(Level.WARNING).log("WorldGen version pack: %s does not contain dir: %s", (Object)packPath, (Object)assetPath);
            return null;
        }
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(filepath);){
            Iterator<Path> it = dirStream.iterator();
            if (!it.hasNext()) {
                LogUtil.getLogger().at(Level.WARNING).log("WorldGen version pack: %s is empty", packPath);
                String string = null;
                return string;
            }
            Path path = it.next();
            if (it.hasNext()) {
                LogUtil.getLogger().at(Level.WARNING).log("WorldGen version pack: %s contains multiple world configs", packPath);
                String string = null;
                return string;
            }
            if (!Files.isDirectory(path, new LinkOption[0])) {
                LogUtil.getLogger().at(Level.WARNING).log("WorldGen version pack: %s does not contain a world config directory", packPath);
                String string = null;
                return string;
            }
            String string = path.getFileName().toString();
            return string;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private static PluginManifest loadManifest(@Nonnull Path manifestPath) throws IOException {
        if (!Files.exists(manifestPath, new LinkOption[0])) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(manifestPath, StandardCharsets.UTF_8);){
            char[] buffer = RawJsonReader.READ_BUFFER.get();
            RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
            ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
            PluginManifest manifest = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
            extraInfo.getValidationResults().logOrThrowValidatorExceptions(LogUtil.getLogger());
            PluginManifest pluginManifest = manifest;
            return pluginManifest;
        }
    }

    public static Path getVersionsPath() {
        return Universe.getWorldGenPath().resolve(VERSIONS_DIR_NAME);
    }

    public record Version(@Nonnull String name, @Nonnull Path path, @Nonnull PluginManifest manifest) implements Comparable<Version>
    {
        @Override
        public int compareTo(Version o) {
            return this.manifest.getVersion().compareTo(o.manifest.getVersion());
        }

        @Nonnull
        public String getPackName() {
            String group = Objects.requireNonNullElse(this.manifest.getGroup(), "Unknown");
            String name = Objects.requireNonNullElse(this.manifest.getName(), "Unknown");
            return group + ":" + name;
        }
    }
}

