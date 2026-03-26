/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.assets.material.OrthogonalRotationAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.ConstantPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.RotatorPattern;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class RotatorPatternAsset
extends PatternAsset {
    @Nonnull
    public static final BuilderCodec<RotatorPatternAsset> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(RotatorPatternAsset.class, RotatorPatternAsset::new, PatternAsset.ABSTRACT_CODEC).append(new KeyedCodec("Pattern", PatternAsset.CODEC, true), (asset, value) -> {
        asset.patternAsset = value;
    }, asset -> asset.patternAsset).add()).append(new KeyedCodec("Rotation", OrthogonalRotationAsset.CODEC, true), (asset, value) -> {
        asset.rotationAsset = value;
    }, asset -> asset.rotationAsset).add()).build();
    @Nonnull
    private PatternAsset patternAsset = new ConstantPatternAsset();
    @Nonnull
    private OrthogonalRotationAsset rotationAsset = new OrthogonalRotationAsset();

    @Override
    @Nonnull
    public Pattern build(@Nonnull PatternAsset.Argument argument) {
        if (super.isSkipped()) {
            return ConstantPattern.INSTANCE_FALSE;
        }
        return new RotatorPattern(this.patternAsset.build(argument), this.rotationAsset.build(), argument.materialCache);
    }

    @Override
    public void cleanUp() {
        this.patternAsset.cleanUp();
    }
}

