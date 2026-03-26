/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.scanners.deprecated;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.math.Calculator;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class AreaScanner
extends Scanner {
    private final int resultCap;
    @Nonnull
    private final Scanner childScanner;
    @Nonnull
    private final List<Vector2i> scanOrder;
    @Nonnull
    private final Bounds3i bounds_voxelGrid;

    public AreaScanner(int resultCap, @Nonnull ScanShape scanShape, int range, @Nonnull Scanner childScanner) {
        if (resultCap < 0 || range < 0) {
            throw new IllegalArgumentException();
        }
        this.resultCap = resultCap;
        this.childScanner = childScanner;
        this.bounds_voxelGrid = childScanner.getBounds_voxelGrid().clone();
        Bounds3i stampBounds_voxelGrid = childScanner.getBounds_voxelGrid().clone();
        ArrayList<Vector2i> scanOrder = new ArrayList<Vector2i>();
        Vector3i position = new Vector3i();
        position.x = -range;
        while (position.x <= range) {
            position.z = -range;
            while (position.z <= range) {
                if (scanShape != ScanShape.CIRCLE || !(Calculator.distance(position.x, position.z, 0.0, 0.0) > (double)range)) {
                    scanOrder.add(new Vector2i(position.x, position.z));
                    stampBounds_voxelGrid.offset(position);
                    this.bounds_voxelGrid.encompass(stampBounds_voxelGrid);
                    stampBounds_voxelGrid.offset(position.scale(-1));
                    position.scale(-1);
                }
                ++position.z;
            }
            ++position.x;
        }
        this.scanOrder = VectorUtil.orderByDistanceFrom(new Vector2i(), scanOrder);
    }

    @Override
    public void scan(@Nonnull Scanner.Context context) {
        if (this.resultCap == 0) {
            return;
        }
        for (Vector2i column : this.scanOrder) {
            Vector3i columnOrigin = new Vector3i(context.position.x + column.x, context.position.y, context.position.z + column.y);
            Scanner.Context childContext = new Scanner.Context(context);
            childContext.position = columnOrigin;
            this.childScanner.scan(childContext);
            if (context.validPositions_out.size() < this.resultCap) continue;
            while (context.validPositions_out.size() > this.resultCap) {
                context.validPositions_out.removeLast();
            }
            return;
        }
    }

    @Override
    public void scan(@NonNullDecl Vector3i anchor, @NonNullDecl Pipe.One<Vector3i> pipe) {
    }

    @Override
    public Bounds3i getBounds_voxelGrid() {
        return this.bounds_voxelGrid;
    }

    public static enum ScanShape {
        CIRCLE,
        SQUARE;

        @Nonnull
        public static final Codec<ScanShape> CODEC;

        static {
            CODEC = new EnumCodec<ScanShape>(ScanShape.class, EnumCodec.EnumStyle.LEGACY);
        }
    }

    public static enum Verticality {
        GLOBAL,
        LOCAL;

    }
}

