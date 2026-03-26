/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class EmptyProp
extends Prop {
    public static final EmptyProp INSTANCE = new EmptyProp();

    @Override
    public boolean generate(@NonNullDecl Prop.Context context) {
        return true;
    }

    @Override
    @NonNullDecl
    public Bounds3i getReadBounds_voxelGrid() {
        return Bounds3i.ZERO;
    }

    @Override
    @NonNullDecl
    public Bounds3i getWriteBounds_voxelGrid() {
        return Bounds3i.ZERO;
    }
}

