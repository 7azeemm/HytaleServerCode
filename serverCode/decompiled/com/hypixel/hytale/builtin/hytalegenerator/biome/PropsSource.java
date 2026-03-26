/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.biome;

import com.hypixel.hytale.builtin.hytalegenerator.PropRuntime;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public interface PropsSource {
    public void getRuntimesWithIndex(int var1, @Nonnull Consumer<PropRuntime> var2);

    public List<PropRuntime> getPropRuntimes();
}

