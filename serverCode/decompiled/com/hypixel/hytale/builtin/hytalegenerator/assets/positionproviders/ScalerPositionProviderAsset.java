/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.ScalerPositionProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class ScalerPositionProviderAsset
extends PositionProviderAsset {
    @Nonnull
    public static final BuilderCodec<ScalerPositionProviderAsset> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ScalerPositionProviderAsset.class, ScalerPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC).append(new KeyedCodec<Vector3d>("Scale", Vector3d.CODEC, true), (asset, v) -> {
        asset.scale = v;
    }, asset -> asset.scale).addValidator((vector, result) -> {
        if (!ScalerPositionProviderAsset.isValidScale(vector)) {
            String msg = "Scale Vector " + vector.toString() + " has one or more zero members.";
            result.fail(msg);
        }
    }).add()).append(new KeyedCodec("Positions", PositionProviderAsset.CODEC, true), (asset, v) -> {
        asset.positionProviderAsset = v;
    }, asset -> asset.positionProviderAsset).add()).build();
    @Nonnull
    private Vector3d scale = new Vector3d();
    @Nonnull
    private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();

    @Override
    @Nonnull
    public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
        if (super.skip() || !ScalerPositionProviderAsset.isValidScale(this.scale)) {
            return EmptyPositionProvider.INSTANCE;
        }
        PositionProvider positionProvider = this.positionProviderAsset.build(argument);
        return new ScalerPositionProvider(this.scale, positionProvider);
    }

    @Override
    public void cleanUp() {
        this.positionProviderAsset.cleanUp();
    }

    private static boolean isValidScale(@Nonnull Vector3d vector) {
        return vector.x != 0.0 && vector.y != 0.0 && vector.z != 0.0;
    }
}

