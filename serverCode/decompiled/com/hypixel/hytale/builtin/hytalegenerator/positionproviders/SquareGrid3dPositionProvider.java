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

public class SquareGrid3dPositionProvider
extends PositionProvider {
    @Nonnull
    private final Vector3d rPosition = new Vector3d();
    @Nonnull
    private final Bounds3d rGridBounds = new Bounds3d();
    @Nonnull
    private final Control rControl = new Control();

    @Override
    public void generate(@NonNullDecl PositionProvider.Context context) {
        this.rGridBounds.min.assign(Math.floor(context.bounds.min.x), Math.floor(context.bounds.min.y), Math.floor(context.bounds.min.z));
        this.rGridBounds.max.assign(Math.ceil(context.bounds.max.x), Math.ceil(context.bounds.max.y), Math.ceil(context.bounds.max.z));
        if (this.rGridBounds.min.x < context.bounds.min.x) {
            this.rGridBounds.min.x += 1.0;
        }
        if (this.rGridBounds.min.y < context.bounds.min.y) {
            this.rGridBounds.min.y += 1.0;
        }
        if (this.rGridBounds.min.z < context.bounds.min.z) {
            this.rGridBounds.min.z += 1.0;
        }
        this.rControl.reset();
        for (double x = this.rGridBounds.min.x; x < this.rGridBounds.max.x; x += 1.0) {
            for (double y = this.rGridBounds.min.y; y < this.rGridBounds.max.y; y += 1.0) {
                for (double z = this.rGridBounds.min.z; z < this.rGridBounds.max.z; z += 1.0) {
                    assert (context.bounds.contains(x, y, z));
                    if (this.rControl.stop) {
                        return;
                    }
                    this.rPosition.assign(x, y, z);
                    context.pipe.accept(this.rPosition, this.rControl);
                }
            }
        }
    }
}

