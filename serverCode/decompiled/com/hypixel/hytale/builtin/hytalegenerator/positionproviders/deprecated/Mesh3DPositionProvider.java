/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.positionproviders.deprecated;

import com.hypixel.hytale.builtin.hytalegenerator.noise.pointprovider.PointProvider;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class Mesh3DPositionProvider
extends PositionProvider {
    @Nonnull
    private final PointProvider pointGenerator;

    public Mesh3DPositionProvider(@Nonnull PointProvider positionProvider) {
        this.pointGenerator = positionProvider;
    }

    @Override
    public void generate(@Nonnull PositionProvider.Context context) {
        Control control = new Control();
        this.pointGenerator.points3d(context.bounds.min, context.bounds.max, position -> context.pipe.accept((Vector3d)position, control));
    }
}

