/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.ConstantPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ConstantPatternAsset
extends PatternAsset {
    @Nonnull
    public static final BuilderCodec<ConstantPatternAsset> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(ConstantPatternAsset.class, ConstantPatternAsset::new, PatternAsset.ABSTRACT_CODEC).append(new KeyedCodec<Boolean>("Value", Codec.BOOLEAN, true), (asset, value) -> {
        asset.value = value;
    }, value -> value.value).add()).build();
    private boolean value = false;

    @Override
    @Nonnull
    public Pattern build(@Nonnull PatternAsset.Argument argument) {
        if (super.isSkipped()) {
            return ConstantPattern.INSTANCE_FALSE;
        }
        return this.value ? ConstantPattern.INSTANCE_TRUE : ConstantPattern.INSTANCE_FALSE;
    }
}

