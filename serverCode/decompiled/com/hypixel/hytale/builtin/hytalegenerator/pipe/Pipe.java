/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.pipe;

import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import javax.annotation.Nonnull;

public class Pipe {
    public static final One<?> EMPTY_ONE = (a, c) -> {};
    public static final Two<?, ?> EMPTY_TWO = (a, b, c) -> {};

    public static <Input> One<Input> getEmptyOne() {
        return EMPTY_ONE;
    }

    public static <InputA, InputB> Two<InputA, InputB> getEmptyTwo() {
        return EMPTY_TWO;
    }

    @FunctionalInterface
    public static interface One<Input> {
        public void accept(@Nonnull Input var1, @Nonnull Control var2);
    }

    @FunctionalInterface
    public static interface Two<InputA, InputB> {
        public void accept(@Nonnull InputA var1, @Nonnull InputB var2, @Nonnull Control var3);
    }
}

