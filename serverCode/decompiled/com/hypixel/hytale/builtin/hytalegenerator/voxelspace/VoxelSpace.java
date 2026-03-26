/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.voxelspace;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface VoxelSpace<T> {
    public void set(@Nullable T var1, int var2, int var3, int var4);

    public void set(@Nullable T var1, @Nonnull Vector3i var2);

    public void setAll(@Nullable T var1);

    @Nullable
    public T get(int var1, int var2, int var3);

    @Nullable
    public T get(@Nonnull Vector3i var1);

    @Nonnull
    public Bounds3i getBounds();
}

