/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.modules.entity.condition;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.condition.Condition;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CheckPlayerGameModeCondition
extends Condition {
    @Nonnull
    public static final BuilderCodec<CheckPlayerGameModeCondition> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(CheckPlayerGameModeCondition.class, CheckPlayerGameModeCondition::new, BASE_CODEC).append(new KeyedCodec<GameMode>("GameMode", new EnumCodec<GameMode>(GameMode.class)), (condition, gameMode) -> {
        condition.gameModeToCheck = gameMode;
    }, condition -> condition.gameModeToCheck).addValidator(Validators.nonNull()).documentation("The game mode to check for. If null, the condition always passes.").add()).build();
    @Nullable
    private GameMode gameModeToCheck;

    protected CheckPlayerGameModeCondition() {
    }

    @Override
    public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
        Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
        if (playerComponent == null) {
            return false;
        }
        return playerComponent.getGameMode() == this.gameModeToCheck;
    }

    @Override
    @Nonnull
    public String toString() {
        return "CheckPlayerGameModeCondition{gameModeToCheck=" + String.valueOf((Object)this.gameModeToCheck) + ", inverse=" + this.inverse + "}";
    }
}

