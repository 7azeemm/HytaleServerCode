/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class WorldMapConfig {
    public static final int ABSOLUTE_MAX_VIEW_RADIUS = 512;
    @Nonnull
    public static final BuilderCodec<WorldMapConfig> ABSTRACT_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.abstractBuilder(WorldMapConfig.class).append(new KeyedCodec<Integer>("ViewRadiusMin", Codec.INTEGER), (o, i) -> {
        o.viewRadiusMin = i;
    }, o -> o.viewRadiusMin).addValidator(Validators.greaterThanOrEqual(0)).add()).append(new KeyedCodec<Integer>("ViewRadiusMax", Codec.INTEGER), (o, i) -> {
        o.viewRadiusMax = i;
    }, o -> o.viewRadiusMax).addValidator(Validators.range(0, 512)).add()).afterDecode(config -> WorldMapConfig.validate(config, 512))).build();
    @Nullable
    protected Integer viewRadiusMin;
    @Nullable
    protected Integer viewRadiusMax;

    public abstract int getDefaultViewRadiusMin();

    public abstract int getDefaultViewRadiusMax();

    public int getViewRadiusMin() {
        return this.viewRadiusMin != null ? this.viewRadiusMin.intValue() : this.getDefaultViewRadiusMin();
    }

    public void setViewRadiusMin(int viewRadiusMin) {
        this.viewRadiusMin = viewRadiusMin;
    }

    public int getViewRadiusMax() {
        return this.viewRadiusMax != null ? this.viewRadiusMax.intValue() : this.getDefaultViewRadiusMax();
    }

    public void setViewRadiusMax(int viewRadiusMax) {
        this.viewRadiusMax = viewRadiusMax;
    }

    protected static void validate(WorldMapConfig config, int ceiling) {
        int max;
        int min = config.getViewRadiusMin();
        if (min > (max = config.getViewRadiusMax())) {
            throw new IllegalArgumentException("ViewRadiusMin (" + min + ") must be less than or equal to ViewRadiusMax (" + max + ")");
        }
        if (max > ceiling) {
            throw new IllegalArgumentException("ViewRadiusMax (" + max + ") must not exceed " + ceiling);
        }
    }
}

