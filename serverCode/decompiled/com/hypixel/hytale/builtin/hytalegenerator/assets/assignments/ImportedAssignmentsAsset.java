/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.assets.assignments;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.assignments.AssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assignments.Assignments;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ImportedAssignmentsAsset
extends AssignmentsAsset {
    @Nonnull
    public static final BuilderCodec<ImportedAssignmentsAsset> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(ImportedAssignmentsAsset.class, ImportedAssignmentsAsset::new, AssignmentsAsset.ABSTRACT_CODEC).append(new KeyedCodec<String>("Name", Codec.STRING, true), (asset, v) -> {
        asset.name = v;
    }, asset -> asset.name).add()).build();
    private String name = "";

    @Override
    public Assignments build(@Nonnull AssignmentsAsset.Argument argument) {
        if (super.skip()) {
            return Assignments.noPropDistribution();
        }
        AssignmentsAsset asset = ImportedAssignmentsAsset.getExportedAsset(this.name);
        if (asset == null) {
            LoggerUtil.getLogger().warning("Couldn't find Assignments asset exported with name: '" + this.name + "'.");
            return Assignments.noPropDistribution();
        }
        return asset.build(argument);
    }
}

