/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers;

import com.hypixel.hytale.builtin.hytalegenerator.ArrayUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.Buffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.performanceinstruments.MemInstrument;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VoxelBuffer<T>
extends Buffer {
    public static final int BUFFER_SIZE_BITS = 3;
    @Nonnull
    public static final Vector3i SIZE = new Vector3i(8, 8, 8);
    @Nonnull
    private static final Bounds3i bounds = new Bounds3i(Vector3i.ZERO, SIZE);
    @Nonnull
    private final Class<T> voxelType;
    @Nonnull
    private State state;
    @Nullable
    private ArrayContents<T> arrayContents;
    @Nullable
    private T singleValue;
    @Nullable
    private VoxelBuffer<T> referenceBuffer;

    public VoxelBuffer(@Nonnull Class<T> voxelType) {
        this.voxelType = voxelType;
        this.state = State.EMPTY;
        this.arrayContents = null;
        this.singleValue = null;
        this.referenceBuffer = null;
    }

    @Nullable
    public T getVoxelContent(int x, int y, int z) {
        assert (bounds.contains(x, y, z));
        return switch (this.state.ordinal()) {
            case 1 -> this.singleValue;
            case 2 -> this.arrayContents.array[VoxelBuffer.index(x, y, z)];
            case 3 -> this.referenceBuffer.getVoxelContent(x, y, z);
            default -> null;
        };
    }

    @Nullable
    public T getVoxelContent(@Nonnull Vector3i position) {
        assert (bounds.contains(position));
        return switch (this.state.ordinal()) {
            case 1 -> this.singleValue;
            case 2 -> this.arrayContents.array[VoxelBuffer.index(position)];
            case 3 -> this.referenceBuffer.getVoxelContent(position);
            default -> null;
        };
    }

    @Nonnull
    public Class<T> getVoxelType() {
        return this.voxelType;
    }

    public void setVoxelContent(int x, int y, int z, @Nullable T value) {
        assert (bounds.contains(x, y, z));
        switch (this.state.ordinal()) {
            case 1: {
                if (this.singleValue == value) {
                    return;
                }
                this.switchFromSingleValueToArray();
                this.setVoxelContent(x, y, z, value);
                break;
            }
            case 2: {
                this.arrayContents.array[VoxelBuffer.index((int)x, (int)y, (int)z)] = value;
                break;
            }
            case 3: {
                this.dereference();
                this.setVoxelContent(x, y, z, value);
                break;
            }
            default: {
                this.state = State.SINGLE_VALUE;
                this.singleValue = value;
            }
        }
    }

    public void setVoxelContent(@Nonnull Vector3i position, @Nullable T value) {
        this.setVoxelContent(position.x, position.y, position.z, value);
    }

    public void reference(@Nonnull VoxelBuffer<T> sourceBuffer) {
        this.state = State.REFERENCE;
        this.referenceBuffer = this.lastReference(sourceBuffer);
        this.singleValue = null;
        this.arrayContents = null;
    }

    @Nonnull
    private VoxelBuffer<T> lastReference(@Nonnull VoxelBuffer<T> sourceBuffer) {
        while (sourceBuffer.state == State.REFERENCE) {
            sourceBuffer = sourceBuffer.referenceBuffer;
        }
        return sourceBuffer;
    }

    @Override
    @Nonnull
    public MemInstrument.Report getMemoryUsage() {
        long size_bytes = 128L;
        size_bytes += 40L;
        if (this.state == State.ARRAY) {
            size_bytes += this.arrayContents.getMemoryUsage().size_bytes();
        }
        return new MemInstrument.Report(size_bytes);
    }

    private void switchFromSingleValueToArray() {
        assert (this.state == State.SINGLE_VALUE);
        this.state = State.ARRAY;
        this.arrayContents = new ArrayContents();
        Arrays.fill(this.arrayContents.array, this.singleValue);
        this.singleValue = null;
    }

    private void dereference() {
        assert (this.state == State.REFERENCE);
        this.state = this.referenceBuffer.state;
        switch (this.state.ordinal()) {
            case 1: {
                this.singleValue = this.referenceBuffer.singleValue;
                break;
            }
            case 2: {
                this.arrayContents = new ArrayContents();
                ArrayUtil.copy(this.referenceBuffer.arrayContents.array, this.arrayContents.array);
                break;
            }
            case 3: {
                this.referenceBuffer = this.referenceBuffer.referenceBuffer;
                break;
            }
            default: {
                return;
            }
        }
    }

    private static int index(int x, int y, int z) {
        return y + x * VoxelBuffer.SIZE.y + z * VoxelBuffer.SIZE.y * VoxelBuffer.SIZE.x;
    }

    private static int index(@Nonnull Vector3i position) {
        return position.y + position.x * VoxelBuffer.SIZE.y + position.z * VoxelBuffer.SIZE.y * VoxelBuffer.SIZE.x;
    }

    private static enum State {
        EMPTY,
        SINGLE_VALUE,
        ARRAY,
        REFERENCE;

    }

    public static class ArrayContents<T>
    implements MemInstrument {
        @Nonnull
        private final T[] array;

        public ArrayContents() {
            this.array = new Object[VoxelBuffer.SIZE.x * VoxelBuffer.SIZE.y * VoxelBuffer.SIZE.z];
        }

        @Override
        @Nonnull
        public MemInstrument.Report getMemoryUsage() {
            long size_bytes = 16L + 8L * (long)this.array.length;
            return new MemInstrument.Report(size_bytes);
        }
    }
}

