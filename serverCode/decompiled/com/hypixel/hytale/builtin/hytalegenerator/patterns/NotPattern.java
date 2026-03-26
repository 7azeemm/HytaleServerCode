/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class NotPattern
extends Pattern {
    @Nonnull
    private final Pattern pattern;

    public NotPattern(@Nonnull Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matches(@Nonnull Pattern.Context context) {
        return !this.pattern.matches(context);
    }

    @Override
    @NonNullDecl
    public Bounds3i getBounds_voxelGrid() {
        return this.pattern.getBounds_voxelGrid();
    }
}

