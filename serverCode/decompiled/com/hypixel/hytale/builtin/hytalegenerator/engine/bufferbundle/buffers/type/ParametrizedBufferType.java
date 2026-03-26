/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type;

import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.Buffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type.BufferType;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class ParametrizedBufferType
extends BufferType {
    @Nonnull
    public final Class parameterClass;

    public ParametrizedBufferType(@Nonnull String name, int index, @Nonnull Class bufferClass, @Nonnull Class parameterClass, @Nonnull Supplier<Buffer> bufferSupplier) {
        super(name, index, bufferClass, bufferSupplier);
        this.parameterClass = parameterClass;
    }

    public boolean isValidType(@Nonnull Class bufferClass, @Nonnull Class parameterClass) {
        return this.bufferClass.equals(bufferClass) && this.parameterClass.equals(parameterClass);
    }

    @Override
    public boolean isValid(@Nonnull Buffer buffer) {
        return this.bufferClass.isInstance(buffer);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParametrizedBufferType)) {
            return false;
        }
        ParametrizedBufferType that = (ParametrizedBufferType)o;
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(this.parameterClass, that.parameterClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.parameterClass);
    }
}

