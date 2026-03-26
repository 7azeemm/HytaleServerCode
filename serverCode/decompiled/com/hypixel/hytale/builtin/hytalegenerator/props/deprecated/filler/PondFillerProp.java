/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.filler;

import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.ScanResult;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.filler.FillerPropScanResult;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PondFillerProp
extends Prop {
    private static final int TRAVERSED = 1;
    private static final int LEAKS = 16;
    private static final int SOLID = 256;
    private static final int STACKED = 4096;
    @Nonnull
    private final Vector3i boundingMin;
    @Nonnull
    private final Vector3i boundingMax;
    @Nonnull
    private final MaterialProvider<Material> fillerMaterialProvider;
    @Nonnull
    private final MaterialSet solidSet;
    @Nonnull
    private final Scanner scanner;
    @Nonnull
    private final Pattern pattern;
    @Nonnull
    private final Bounds3i readBounds_voxelGrid;
    @Nonnull
    private final Bounds3i writeBounds_voxelGrid;

    public PondFillerProp(@Nonnull Vector3i boundingMin, @Nonnull Vector3i boundingMax, @Nonnull MaterialSet solidSet, @Nonnull MaterialProvider<Material> fillerMaterialProvider, @Nonnull Scanner scanner, @Nonnull Pattern pattern) {
        this.boundingMin = boundingMin.clone();
        this.boundingMax = boundingMax.clone();
        this.solidSet = solidSet;
        this.fillerMaterialProvider = fillerMaterialProvider;
        this.scanner = scanner;
        this.pattern = pattern;
        this.readBounds_voxelGrid = this.scanner.getBoundsWithPattern_voxelGrid(pattern);
        this.writeBounds_voxelGrid = new Bounds3i(boundingMin, boundingMax);
        this.writeBounds_voxelGrid.stack(this.readBounds_voxelGrid);
    }

    @Nonnull
    public FillerPropScanResult scan_deprecated(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace) {
        Scanner.Context scannerContext = new Scanner.Context(position, this.pattern, materialSpace, new ArrayList<Vector3i>());
        this.scanner.scan(scannerContext);
        if (scannerContext.validPositions_out.size() == 1) {
            List<Vector3i> resultList = this.renderFluidBlocks(scannerContext.validPositions_out.getFirst(), materialSpace);
            return new FillerPropScanResult(resultList);
        }
        ArrayList<Vector3i> resultList = new ArrayList<Vector3i>();
        for (Vector3i scanPosition : scannerContext.validPositions_out) {
            List<Vector3i> renderResult = this.renderFluidBlocks(scanPosition, materialSpace);
            resultList.addAll(renderResult);
        }
        return new FillerPropScanResult(resultList);
    }

    @Nonnull
    private List<Vector3i> renderFluidBlocks(@Nonnull Vector3i origin, @Nonnull VoxelSpace<Material> materialSpace) {
        int z;
        int x;
        int contextMaterialHash;
        Material material;
        Vector3i min = this.boundingMin.clone().add(origin);
        Vector3i max = this.boundingMax.clone().add(origin);
        min = Vector3i.max(min, materialSpace.getBounds().min);
        max = Vector3i.min(max, materialSpace.getBounds().max);
        Bounds3i maskBounds = new Bounds3i(min, max);
        ArrayVoxelSpace<Integer> mask = new ArrayVoxelSpace<Integer>(new Bounds3i(min, max));
        mask.setAll(0);
        int y = min.y;
        for (int x2 = min.x; x2 < max.x; ++x2) {
            for (int z2 = min.z; z2 < max.z; ++z2) {
                material = materialSpace.get(x2, y, z2);
                contextMaterialHash = material.hashMaterialIds();
                int maskValue = 1;
                if (this.solidSet.test(contextMaterialHash)) {
                    mask.set(maskValue |= 0x100, x2, y, z2);
                    continue;
                }
                mask.set(maskValue |= 0x10, x2, y, z2);
            }
        }
        for (y = min.y + 1; y < max.y; ++y) {
            int underY = y - 1;
            for (x = min.x; x < max.x; ++x) {
                for (z = min.z; z < max.z; ++z) {
                    if (PondFillerProp.isTraversed((Integer)mask.get(x, y, z))) continue;
                    int maskValueUnder = (Integer)mask.get(x, underY, z);
                    material = materialSpace.get(x, y, z);
                    contextMaterialHash = material.hashMaterialIds();
                    if (this.solidSet.test(contextMaterialHash)) {
                        int maskValue = 0;
                        maskValue |= 1;
                        mask.set(maskValue |= 0x100, x, y, z);
                        continue;
                    }
                    if (!PondFillerProp.isLeaks(maskValueUnder) && x != min.x && x != max.x - 1 && z != min.z && z != max.z - 1) continue;
                    ArrayDeque<Vector3i> stack = new ArrayDeque<Vector3i>();
                    stack.push(new Vector3i(x, y, z));
                    mask.set(4096, x, y, z);
                    while (!stack.isEmpty()) {
                        int poppedMaskValue;
                        Vector3i poppedPos = (Vector3i)stack.pop();
                        int maskValue = (Integer)mask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                        mask.set(maskValue |= 0x10, poppedPos.x, poppedPos.y, poppedPos.z);
                        --poppedPos.x;
                        if (mask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z) && !PondFillerProp.isStacked(poppedMaskValue = ((Integer)mask.get(poppedPos.x, poppedPos.y, poppedPos.z)).intValue()) && !this.solidSet.test(contextMaterialHash = (material = materialSpace.get(poppedPos.x, poppedPos.y, poppedPos.z)).hashMaterialIds())) {
                            stack.push(poppedPos.clone());
                            mask.set(0x1000 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                        }
                        poppedPos.x += 2;
                        if (mask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z) && !PondFillerProp.isStacked(poppedMaskValue = ((Integer)mask.get(poppedPos.x, poppedPos.y, poppedPos.z)).intValue()) && !this.solidSet.test(contextMaterialHash = (material = materialSpace.get(poppedPos.x, poppedPos.y, poppedPos.z)).hashMaterialIds())) {
                            stack.push(poppedPos.clone());
                            mask.set(0x1000 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                        }
                        --poppedPos.x;
                        --poppedPos.z;
                        if (mask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z) && !PondFillerProp.isStacked(poppedMaskValue = ((Integer)mask.get(poppedPos.x, poppedPos.y, poppedPos.z)).intValue()) && !this.solidSet.test(contextMaterialHash = (material = materialSpace.get(poppedPos.x, y, poppedPos.z)).hashMaterialIds())) {
                            stack.push(poppedPos.clone());
                            mask.set(0x1000 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                        }
                        poppedPos.z += 2;
                        if (mask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z) && !PondFillerProp.isStacked(poppedMaskValue = ((Integer)mask.get(poppedPos.x, poppedPos.y, poppedPos.z)).intValue()) && !this.solidSet.test(contextMaterialHash = (material = materialSpace.get(poppedPos.x, poppedPos.y, poppedPos.z)).hashMaterialIds())) {
                            stack.push(poppedPos.clone());
                            mask.set(0x1000 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                        }
                        --poppedPos.z;
                    }
                }
            }
        }
        ArrayList<Vector3i> fluidBlocks = new ArrayList<Vector3i>();
        for (y = maskBounds.min.y + 1; y < maskBounds.max.y; ++y) {
            for (x = maskBounds.min.x + 1; x < maskBounds.max.x - 1; ++x) {
                for (z = maskBounds.min.z + 1; z < maskBounds.max.z - 1; ++z) {
                    int maskValue = (Integer)mask.get(x, y, z);
                    if (PondFillerProp.isSolid(maskValue) || PondFillerProp.isLeaks(maskValue)) continue;
                    fluidBlocks.add(new Vector3i(x, y, z));
                }
            }
        }
        return fluidBlocks;
    }

    public void place_deprecated(@Nonnull Prop.Context context, @Nonnull ScanResult scanResult) {
        List<Vector3i> fluidBlocks = FillerPropScanResult.cast(scanResult).getFluidBlocks();
        if (fluidBlocks == null) {
            return;
        }
        for (Vector3i position : fluidBlocks) {
            MaterialProvider.Context materialsContext;
            Material material;
            if (!context.materialWriteSpace.getBounds().contains(position.x, position.y, position.z) || (material = this.fillerMaterialProvider.getVoxelTypeAt(materialsContext = new MaterialProvider.Context(position, 0.0, 0, 0, 0, 0, null, context.distanceToBiomeEdge))) == null) continue;
            context.materialWriteSpace.set(material, position.x, position.y, position.z);
        }
    }

    @Override
    public boolean generate(@NonNullDecl Prop.Context context) {
        FillerPropScanResult scanResult = this.scan_deprecated(context.position, context.materialReadSpace);
        this.place_deprecated(context, scanResult);
        return !scanResult.isNegative();
    }

    @Override
    @NonNullDecl
    public Bounds3i getReadBounds_voxelGrid() {
        return this.readBounds_voxelGrid;
    }

    @Override
    @Nonnull
    public Bounds3i getWriteBounds_voxelGrid() {
        return this.writeBounds_voxelGrid;
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

