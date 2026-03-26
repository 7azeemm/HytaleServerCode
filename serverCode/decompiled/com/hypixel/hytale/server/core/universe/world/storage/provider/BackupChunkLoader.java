/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.provider.IChunkStorageProvider;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nonnull;

public class BackupChunkLoader
implements IChunkLoader {
    private final List<IChunkLoader> loaders = new ArrayList<IChunkLoader>();
    private final List<FileSystem> fileSystems = new ArrayList<FileSystem>();
    private final List<Path> tempDirs = new ArrayList<Path>();

    public BackupChunkLoader(ChunkStore store, List<Path> backups) throws IOException {
        IChunkStorageProvider<?> baseProvider = store.getWorld().getWorldConfig().getChunkStorageProvider();
        Path worldPath = Universe.get().getWorldsPath();
        for (Path path : backups) {
            Path loaderPath;
            Path savePath = store.getWorld().getSavePath();
            String relWorldPath = worldPath.relativize(savePath).toString().replace('\\', '/');
            String expectedChunksPrefix = "worlds/" + relWorldPath + "/chunks/";
            FileSystem fs = FileSystems.newFileSystem(path);
            Path worldBackupPath = fs.getPath("worlds", worldPath.relativize(savePath).toString());
            if (Files.exists(worldBackupPath, new LinkOption[0])) {
                this.fileSystems.add(fs);
                loaderPath = worldBackupPath;
            } else {
                fs.close();
                Path tempDir = Files.createTempDirectory("hytale-backup-recovery-", new FileAttribute[0]);
                this.tempDirs.add(tempDir);
                Path chunksDir = tempDir.resolve("chunks");
                Files.createDirectory(chunksDir, new FileAttribute[0]);
                try (ZipFile zipFile = new ZipFile(path.toFile());){
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        String fileName;
                        ZipEntry entry = entries.nextElement();
                        String normalized = entry.getName().replace('\\', '/');
                        if (entry.isDirectory() || !normalized.startsWith(expectedChunksPrefix) || (fileName = normalized.substring(expectedChunksPrefix.length())).contains("/")) continue;
                        Path target = chunksDir.resolve(fileName);
                        InputStream in = zipFile.getInputStream(entry);
                        try {
                            Files.copy(in, target, new CopyOption[0]);
                        }
                        finally {
                            if (in == null) continue;
                            in.close();
                        }
                    }
                }
                loaderPath = tempDir;
            }
            IChunkLoader loader = baseProvider.getRecoveryLoader(store.getStore(), loaderPath);
            if (loader == null) {
                throw new RuntimeException("Recovery of individual chunks from backups not supported by storage type. Please restore instead.");
            }
            this.loaders.add(loader);
        }
    }

    @Override
    @Nonnull
    public CompletableFuture<Holder<ChunkStore>> loadHolder(int x, int z) {
        return this.loadChunkNext(this.loaders.iterator(), x, z);
    }

    private CompletableFuture<Holder<ChunkStore>> loadChunkNext(Iterator<IChunkLoader> iterator, int x, int z) {
        if (!iterator.hasNext()) {
            return CompletableFuture.completedFuture(null);
        }
        IChunkLoader loader = iterator.next();
        return loader.loadHolder(x, z).exceptionallyCompose(t -> this.loadChunkNext(iterator, x, z));
    }

    @Override
    @Nonnull
    public LongSet getIndexes() throws IOException {
        return LongSet.of();
    }

    @Override
    public void close() throws IOException {
        for (IChunkLoader loader : this.loaders) {
            loader.close();
        }
        for (FileSystem fs : this.fileSystems) {
            fs.close();
        }
        for (Path tempDir : this.tempDirs) {
            Stream<Path> walk = Files.walk(tempDir, new FileVisitOption[0]);
            try {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                });
            }
            finally {
                if (walk == null) continue;
                walk.close();
            }
        }
    }
}

