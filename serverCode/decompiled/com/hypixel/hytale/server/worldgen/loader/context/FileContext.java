/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.worldgen.loader.context;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nonnull;

public class FileContext<T extends FileContext<?>> {
    private final int id;
    @Nonnull
    private final String name;
    @Nonnull
    private final Path filepath;
    @Nonnull
    private final T parentContext;
    private transient String rootPath = null;
    private transient String contentPath = null;

    public FileContext(int id, @Nonnull String name, @Nonnull Path filepath, @Nonnull T parentContext) {
        this.id = id;
        this.name = name;
        this.filepath = filepath;
        this.parentContext = parentContext;
    }

    public int getId() {
        return this.id;
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nonnull
    public Path getPath() {
        return this.filepath;
    }

    @Nonnull
    public String getRootPath() {
        if (this.rootPath == null) {
            this.rootPath = this.getRoot().filepath.getFileName().toString();
        }
        return this.rootPath;
    }

    @Nonnull
    public T getParentContext() {
        return this.parentContext;
    }

    @Nonnull
    public String getContentPath() {
        if (this.contentPath == null) {
            this.contentPath = FileContext.toContentPath(this.filepath, this.parentContext);
        }
        return this.contentPath;
    }

    @Nonnull
    public FileContext<?> getRoot() {
        FileContext<T> context = this;
        while (context.parentContext != RootContext.INSTANCE) {
            context = context.parentContext;
        }
        return context;
    }

    @Nonnull
    private static String toContentPath(@Nonnull Path filepath, @Nonnull FileContext<?> parent) {
        int start;
        StringBuilder sb = new StringBuilder();
        for (int i = start = parent == RootContext.INSTANCE ? 0 : parent.getRoot().filepath.getNameCount(); i < filepath.getNameCount(); ++i) {
            int ext;
            if (i > start) {
                sb.append('.');
            }
            String name = filepath.getName(i).toString();
            int end = name.length();
            if (i == filepath.getNameCount() - 1 && (ext = name.lastIndexOf(46)) != -1) {
                end = ext;
            }
            sb.append(name, 0, end);
        }
        return sb.toString();
    }

    public static class RootContext
    extends FileContext<RootContext> {
        public static final RootContext INSTANCE = new RootContext();

        private RootContext() {
            super(-1, ".", Paths.get(".", new String[0]), null);
        }
    }

    public static interface Constants {
        public static final String ERROR_MISSING_ENTRY = "Missing %s entry for key %s";
        public static final String ERROR_DUPLICATE_ENTRY = "Duplicate %s entry registered for key %s";
    }

    public static class Registry<T>
    implements Iterable<Map.Entry<String, T>> {
        private final String registryName;
        @Nonnull
        private final Object2ObjectMap<String, T> backing;

        public Registry(String name) {
            this.registryName = name;
            this.backing = new Object2ObjectLinkedOpenHashMap<String, T>();
        }

        public int size() {
            return this.backing.size();
        }

        public String getName() {
            return this.registryName;
        }

        public boolean contains(String name) {
            return this.backing.containsKey(name);
        }

        @Nonnull
        public T get(String name) {
            Object value = this.backing.get(name);
            if (value == null) {
                throw new Error(String.format("Missing %s entry for key %s", this.registryName, name));
            }
            return (T)value;
        }

        public void register(String name, T biome) {
            if (this.backing.containsKey(name)) {
                throw new Error(String.format("Duplicate %s entry registered for key %s", this.registryName, name));
            }
            this.backing.put(name, biome);
        }

        @Override
        @Nonnull
        public Iterator<Map.Entry<String, T>> iterator() {
            return this.backing.entrySet().iterator();
        }
    }
}

