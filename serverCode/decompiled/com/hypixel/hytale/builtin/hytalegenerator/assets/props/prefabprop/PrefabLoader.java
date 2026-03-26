/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.PrefabFileVisitor;
import com.hypixel.hytale.common.util.ExceptionUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabLoader {
    public static void traverseAllPrefabBuffersUnder(@Nonnull Path path, @Nonnull BiConsumer<Path, IPrefabBuffer> prefabsOut) {
        if (!Files.isDirectory(path, new LinkOption[0])) {
            IPrefabBuffer prefab = PrefabLoader.loadPrefabBufferAt(path);
            if (prefab == null) {
                return;
            }
            prefabsOut.accept(path, prefab);
            return;
        }
        try {
            Files.walkFileTree(path, new PrefabFileVisitor(prefabsOut));
        }
        catch (IOException e) {
            Object msg = "Exception thrown by HytaleGenerator while loading a Prefab:\n";
            msg = (String)msg + ExceptionUtil.toStringWithStack(e);
            LoggerUtil.getLogger().severe((String)msg);
        }
    }

    @Nullable
    public static IPrefabBuffer loadPrefabBufferAt(@Nonnull Path filePath) {
        if (!PrefabLoader.hasJsonExtension(filePath)) {
            return null;
        }
        if (!Files.exists(filePath, new LinkOption[0])) {
            LoggerUtil.getLogger().info("Didn't find a prefab with path: " + String.valueOf(filePath));
            return null;
        }
        try {
            return PrefabBufferUtil.getCached(filePath);
        }
        catch (Error e) {
            return null;
        }
    }

    public static boolean hasJsonExtension(@Nonnull Path path) {
        String pathString = path.toString();
        return pathString.toLowerCase().endsWith(".json");
    }
}

