/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class TriangularGrid2dPositionProvider
extends PositionProvider {
    private static final double Y = 0.0;
    private static final double SPACING = 1.0;
    private static final double HALF_SPACING = 0.5;
    private static final double X_HEIGHT = Math.sqrt(0.75);
    private static final double X_HEIGHT_INVERSE = 1.0 / X_HEIGHT;
    @Nonnull
    private final Vector3d rPosition = new Vector3d();
    @Nonnull
    private final Bounds3d rGridBounds = new Bounds3d();
    @Nonnull
    private final Control rControl = new Control();

    @Override
    public void generate(@NonNullDecl PositionProvider.Context context) {
        if (context.bounds.min.y > 0.0 || context.bounds.max.y <= 0.0) {
            return;
        }
        this.rGridBounds.min.assign(Math.floor(context.bounds.min.x), 0.0, Math.floor(context.bounds.min.z));
        this.rGridBounds.max.assign(Math.ceil(context.bounds.max.x), 1.0, Math.ceil(context.bounds.max.z));
        if (this.rGridBounds.min.x < context.bounds.min.x) {
            this.rGridBounds.min.x += 1.0;
        }
        if (this.rGridBounds.min.z < context.bounds.min.z) {
            this.rGridBounds.min.z += 1.0;
        }
        this.rGridBounds.min.x *= X_HEIGHT_INVERSE;
        this.rGridBounds.max.x *= X_HEIGHT_INVERSE;
        this.rControl.reset();
        int x = (int)Math.floor(this.rGridBounds.min.x);
        while ((double)x < this.rGridBounds.max.x) {
            double zOffset = x % 2 == 0 ? 0.5 : 0.0;
            for (double z = this.rGridBounds.min.z - zOffset; z < this.rGridBounds.max.z; z += 1.0) {
                this.rPosition.assign((double)x * X_HEIGHT, 0.0, z);
                if (!context.bounds.contains(this.rPosition)) continue;
                if (this.rControl.stop) {
                    return;
                }
                context.pipe.accept(this.rPosition, this.rControl);
            }
            ++x;
        }
    }

    private static double toX0(double position) {
        return TriangularGrid2dPositionProvider.toCellGrid(position) * X_HEIGHT;
    }

    private static double toCellGrid(double position) {
        return Math.floor(position * X_HEIGHT_INVERSE);
    }
}

