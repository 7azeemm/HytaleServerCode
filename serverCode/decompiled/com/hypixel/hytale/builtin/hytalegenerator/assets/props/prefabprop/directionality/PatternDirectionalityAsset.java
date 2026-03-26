/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality.DirectionalityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.ConstantPattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality.Directionality;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality.OrthogonalDirection;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality.PatternDirectionality;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class PatternDirectionalityAsset
extends DirectionalityAsset {
    @Nonnull
    public static final BuilderCodec<PatternDirectionalityAsset> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(PatternDirectionalityAsset.class, PatternDirectionalityAsset::new, DirectionalityAsset.ABSTRACT_CODEC).append(new KeyedCodec<OrthogonalDirection>("InitialDirection", OrthogonalDirection.CODEC, true), (asset, v) -> {
        asset.prefabDirection = v;
    }, asset -> asset.prefabDirection).add()).append(new KeyedCodec<String>("Seed", Codec.STRING, true), (asset, v) -> {
        asset.seed = v;
    }, asset -> asset.seed).add()).append(new KeyedCodec("NorthPattern", PatternAsset.CODEC, true), (asset, v) -> {
        asset.northPatternAsset = v;
    }, asset -> asset.northPatternAsset).add()).append(new KeyedCodec("SouthPattern", PatternAsset.CODEC, true), (asset, v) -> {
        asset.southPatternAsset = v;
    }, asset -> asset.southPatternAsset).add()).append(new KeyedCodec("EastPattern", PatternAsset.CODEC, true), (asset, v) -> {
        asset.eastPatternAsset = v;
    }, asset -> asset.eastPatternAsset).add()).append(new KeyedCodec("WestPattern", PatternAsset.CODEC, true), (asset, v) -> {
        asset.westPatternAsset = v;
    }, asset -> asset.westPatternAsset).add()).build();
    private String seed = "A";
    private OrthogonalDirection prefabDirection = OrthogonalDirection.N;
    private PatternAsset northPatternAsset = new ConstantPatternAsset();
    private PatternAsset southPatternAsset = new ConstantPatternAsset();
    private PatternAsset eastPatternAsset = new ConstantPatternAsset();
    private PatternAsset westPatternAsset = new ConstantPatternAsset();

    @Override
    @Nonnull
    public Directionality build(@Nonnull DirectionalityAsset.Argument argument) {
        int intSeed = argument.parentSeed.child(this.seed).createSupplier().get();
        OrthogonalDirection direction = this.prefabDirection;
        ConstantPattern northPattern = this.northPatternAsset == null ? ConstantPattern.INSTANCE_FALSE : this.northPatternAsset.build(PatternAsset.argumentFrom(argument));
        ConstantPattern southPattern = this.southPatternAsset == null ? ConstantPattern.INSTANCE_FALSE : this.southPatternAsset.build(PatternAsset.argumentFrom(argument));
        ConstantPattern eastPattern = this.eastPatternAsset == null ? ConstantPattern.INSTANCE_FALSE : this.eastPatternAsset.build(PatternAsset.argumentFrom(argument));
        ConstantPattern westPattern = this.westPatternAsset == null ? ConstantPattern.INSTANCE_FALSE : this.westPatternAsset.build(PatternAsset.argumentFrom(argument));
        return new PatternDirectionality(direction, southPattern, northPattern, eastPattern, westPattern, intSeed);
    }

    @Override
    public void cleanUp() {
        this.northPatternAsset.cleanUp();
        this.southPatternAsset.cleanUp();
        this.eastPatternAsset.cleanUp();
        this.westPatternAsset.cleanUp();
    }
}

