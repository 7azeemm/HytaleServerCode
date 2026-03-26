/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayDeque;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PondFillerProp
extends Prop {
    private static final int TRAVERSED = 1;
    private static final int LEAKS = 16;
    private static final int SOLID = 256;
    private static final int STACKED = 4096;
    @Nonnull
    private final Bounds3i bounds;
    @Nonnull
    private final MaterialProvider<Material> fillerMaterialProvider;
    @Nonnull
    private final MaterialSet solidSet;
    @Nonnull
    private final Bounds3i rLocalBounds;
    @Nonnull
    private final Bounds3i rLocalWriteBounds;
    @Nonnull
    private final ArrayVoxelSpace<Integer> rMask;
    @Nonnull
    private final MaterialProvider.Context rMaterialProviderContext;

    public PondFillerProp(@Nonnull Bounds3i bounds, @Nonnull MaterialProvider<Material> fillerMaterialProvider, @Nonnull MaterialSet solidSet) {
        this.bounds = bounds.clone();
        this.fillerMaterialProvider = fillerMaterialProvider;
        this.solidSet = solidSet;
        this.rLocalBounds = new Bounds3i();
        this.rLocalWriteBounds = new Bounds3i();
        this.rMask = new ArrayVoxelSpace(bounds);
        this.rMaterialProviderContext = new MaterialProvider.Context(new Vector3i(), 0.0, 0, 0, 0, 0, null, Double.MAX_VALUE);
    }

    @Override
    public boolean generate(@NonNullDecl Prop.Context context) {
        int z;
        int x;
        this.rLocalBounds.assign(this.bounds).offset(context.position);
        this.rLocalWriteBounds.assign(this.rLocalBounds).intersect(context.materialWriteSpace.getBounds());
        if (!context.materialReadSpace.getBounds().contains(this.rLocalBounds)) {
            return true;
        }
        Bounds3i localMaskBounds = this.rMask.getBounds();
        localMaskBounds.assign(this.bounds);
        localMaskBounds.offset(context.position);
        this.rMask.setAll(0);
        int y = this.rLocalBounds.min.y;
        for (x = this.rLocalBounds.min.x; x < this.rLocalBounds.max.x; ++x) {
            for (z = this.rLocalBounds.min.z; z < this.rLocalBounds.max.z; ++z) {
                Material material = context.materialReadSpace.get(x, y, z);
                int contextMaterialHash = material.hashMaterialIds();
                int maskValue = 1;
                if (this.solidSet.test(contextMaterialHash)) {
                    this.rMask.set(maskValue |= 0x100, x, y, z);
                    continue;
                }
                this.rMask.set(maskValue |= 0x10, x, y, z);
            }
        }
        for (y = this.rLocalBounds.min.y + 1; y < this.rLocalBounds.max.y; ++y) {
            int underY = y - 1;
            for (int x2 = this.rLocalBounds.min.x; x2 < this.rLocalBounds.max.x; ++x2) {
                for (int z2 = this.rLocalBounds.min.z; z2 < this.rLocalBounds.max.z; ++z2) {
                    if (PondFillerProp.isTraversed(this.rMask.get(x2, y, z2))) continue;
                    int maskValueUnder = this.rMask.get(x2, underY, z2);
                    Material material = context.materialReadSpace.get(x2, y, z2);
                    int contextMaterialHash = material.hashMaterialIds();
                    if (this.solidSet.test(contextMaterialHash)) {
                        int maskValue = 0;
                        maskValue |= 1;
                        this.rMask.set(maskValue |= 0x100, x2, y, z2);
                        continue;
                    }
                    if (!PondFillerProp.isLeaks(maskValueUnder) && x2 != this.rLocalBounds.min.x && x2 != this.rLocalBounds.max.x - 1 && z2 != this.rLocalBounds.min.z && z2 != this.rLocalBounds.max.z - 1) continue;
                    ArrayDeque<Vector3i> stack = new ArrayDeque<Vector3i>();
                    stack.push(new Vector3i(x2, y, z2));
                    this.rMask.set(4096, x2, y, z2);
                    while (!stack.isEmpty()) {
                        int poppedMaskValue;
                        Vector3i poppedPos = (Vector3i)stack.pop();
                        int maskValue = this.rMask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                        this.rMask.set(maskValue |= 0x10, poppedPos.x, poppedPos.y, poppedPos.z);
                        --poppedPos.x;
                        if (this.rMask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z) && !PondFillerProp.isStacked(poppedMaskValue = this.rMask.get(poppedPos.x, poppedPos.y, poppedPos.z).intValue()) && !this.solidSet.test(contextMaterialHash = (material = context.materialReadSpace.get(poppedPos.x, poppedPos.y, poppedPos.z)).hashMaterialIds())) {
                            stack.push(poppedPos.clone());
                            this.rMask.set(0x1000 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                        }
                        poppedPos.x += 2;
                        if (this.rMask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z) && !PondFillerProp.isStacked(poppedMaskValue = this.rMask.get(poppedPos.x, poppedPos.y, poppedPos.z).intValue()) && !this.solidSet.test(contextMaterialHash = (material = context.materialReadSpace.get(poppedPos.x, poppedPos.y, poppedPos.z)).hashMaterialIds())) {
                            stack.push(poppedPos.clone());
                            this.rMask.set(0x1000 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                        }
                        --poppedPos.x;
                        --poppedPos.z;
                        if (this.rMask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z) && !PondFillerProp.isStacked(poppedMaskValue = this.rMask.get(poppedPos.x, poppedPos.y, poppedPos.z).intValue()) && !this.solidSet.test(contextMaterialHash = (material = context.materialReadSpace.get(poppedPos.x, y, poppedPos.z)).hashMaterialIds())) {
                            stack.push(poppedPos.clone());
                            this.rMask.set(0x1000 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                        }
                        poppedPos.z += 2;
                        if (this.rMask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z) && !PondFillerProp.isStacked(poppedMaskValue = this.rMask.get(poppedPos.x, poppedPos.y, poppedPos.z).intValue()) && !this.solidSet.test(contextMaterialHash = (material = context.materialReadSpace.get(poppedPos.x, poppedPos.y, poppedPos.z)).hashMaterialIds())) {
                            stack.push(poppedPos.clone());
                            this.rMask.set(0x1000 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                        }
                        --poppedPos.z;
                    }
                }
            }
        }
        this.rMaterialProviderContext.distanceToBiomeEdge = context.distanceToBiomeEdge;
        for (y = this.rLocalWriteBounds.min.y; y < this.rLocalWriteBounds.max.y; ++y) {
            for (x = this.rLocalWriteBounds.min.x; x < this.rLocalWriteBounds.max.x; ++x) {
                for (z = this.rLocalWriteBounds.min.z; z < this.rLocalWriteBounds.max.z; ++z) {
                    int maskValue = this.rMask.get(x, y, z);
                    if (PondFillerProp.isSolid(maskValue) || PondFillerProp.isLeaks(maskValue)) continue;
                    this.rMaterialProviderContext.position.assign(x, y, z);
                    Material material = this.fillerMaterialProvider.getVoxelTypeAt(this.rMaterialProviderContext);
                    context.materialWriteSpace.set(material, x, y, z);
                }
            }
        }
        return true;
    }

    @Override
    @NonNullDecl
    public Bounds3i getReadBounds_voxelGrid() {
        return this.bounds;
    }

    @Override
    @NonNullDecl
    public Bounds3i getWriteBounds_voxelGrid() {
        return this.bounds;
    }

    private static boolean isTraversed(int maskValue) {
        return (maskValue & 1) == 1;
    }

    private static boolean isLeaks(int maskValue) {
        return (maskValue & 0x10) == 16;
    }

    private static boolean isSolid(int maskValue) {
        return (maskValue & 0x100) == 256;
    }

    private static boolean isStacked(int maskValue) {
        return (maskValue & 0x1000) == 4096;
    }
}

