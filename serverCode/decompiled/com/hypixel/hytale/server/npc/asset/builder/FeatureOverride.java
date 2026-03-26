/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.npc.asset.builder;

import java.util.function.Supplier;

public enum FeatureOverride implements Supplier<String>
{
    On("Feature always enabled"),
    Off("Feature always disabled"),
    Default("Default behaviour");

    private final String description;

    private FeatureOverride(String description) {
        this.description = description;
    }

    @Override
    public String get() {
        return this.description;
    }

    public boolean evaluate(boolean defaultValue) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> true;
            case 1 -> false;
            case 2 -> defaultValue;
        };
    }
}

