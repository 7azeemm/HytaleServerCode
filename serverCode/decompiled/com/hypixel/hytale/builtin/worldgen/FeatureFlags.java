/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.worldgen;

import com.hypixel.hytale.server.worldgen.util.LogUtil;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public interface FeatureFlags {
    @Deprecated(since="2026-01-19", forRemoval=true)
    public static final boolean VERSION_OVERRIDES = FeatureFlags.of("hytale.worldgen.version_overrides");

    public static boolean of(@Nonnull String featureFlag) {
        if (System.getProperty(featureFlag) != null || System.getenv(featureFlag) != null) {
            LogUtil.getLogger().at(Level.INFO).log("Feature %s is enabled.", featureFlag);
            return true;
        }
        return false;
    }
}

