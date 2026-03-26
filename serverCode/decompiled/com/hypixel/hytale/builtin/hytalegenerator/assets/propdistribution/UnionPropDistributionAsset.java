/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution;

import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.PropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.NoPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.UnionPropDistribution;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class UnionPropDistributionAsset
extends PropDistributionAsset {
    @Nonnull
    public static final BuilderCodec<UnionPropDistributionAsset> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(UnionPropDistributionAsset.class, UnionPropDistributionAsset::new, PropDistributionAsset.ABSTRACT_CODEC).append(new KeyedCodec<T[]>("PropDistributions", new ArrayCodec(PropDistributionAsset.CODEC, PropDistributionAsset[]::new), true), (asset, value) -> {
        asset.propDistributionAssets = value;
    }, asset -> asset.propDistributionAssets).add()).build();
    @Nonnull
    private PropDistributionAsset[] propDistributionAssets = new PropDistributionAsset[0];

    @Override
    @Nonnull
    public PropDistribution build(@Nonnull PropDistributionAsset.Argument argument) {
        if (super.isSkipped()) {
            return NoPropDistribution.INSTANCE;
        }
        ArrayList<PropDistribution> propDistributions = new ArrayList<PropDistribution>(this.propDistributionAssets.length);
        for (PropDistributionAsset asset : this.propDistributionAssets) {
            propDistributions.add(asset.build(argument));
        }
        return new UnionPropDistribution(propDistributions);
    }
}

