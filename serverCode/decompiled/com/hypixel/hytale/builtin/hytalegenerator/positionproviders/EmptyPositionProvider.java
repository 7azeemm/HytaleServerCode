/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class EmptyPositionProvider
extends PositionProvider {
    public static final EmptyPositionProvider INSTANCE = new EmptyPositionProvider();

    @Override
    public void generate(@NonNullDecl PositionProvider.Context context) {
    }
}

