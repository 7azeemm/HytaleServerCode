/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.npc.instructions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.instructions.Motion;
import javax.annotation.Nullable;

public interface BodyMotion
extends Motion {
    @Nullable
    default public BodyMotion getSteeringMotion() {
        return this;
    }

    default public double getDesiredTargetDistance() {
        return Double.MAX_VALUE;
    }

    @Nullable
    default public Ref<EntityStore> getDesiredTargetEntity() {
        return null;
    }
}

