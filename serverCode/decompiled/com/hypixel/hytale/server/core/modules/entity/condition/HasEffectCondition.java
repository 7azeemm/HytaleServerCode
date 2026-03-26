/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.modules.entity.condition;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.condition.Condition;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HasEffectCondition
extends Condition {
    @Nonnull
    public static final BuilderCodec<HasEffectCondition> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(HasEffectCondition.class, HasEffectCondition::new, Condition.BASE_CODEC).appendInherited(new KeyedCodec<String>("EffectId", Codec.STRING), (condition, effectId) -> {
        condition.entityEffectId = effectId;
    }, condition -> condition.entityEffectId, (condition, parent) -> {
        condition.entityEffectId = parent.entityEffectId;
    }).addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late()).documentation("The effect to check for. Returns true if the entity has an active effect matching this one. If null, this condition always returns false.").add()).build();
    @Nullable
    private String entityEffectId;
    @Nullable
    private EntityEffect entityEffect;

    protected HasEffectCondition() {
    }

    @Override
    public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
        EffectControllerComponent effectControllerComponent;
        if (this.entityEffect == null) {
            if (this.entityEffectId == null || this.entityEffectId.isEmpty()) {
                return false;
            }
            this.entityEffect = (EntityEffect)EntityEffect.getAssetMap().getAsset(this.entityEffectId);
        }
        if ((effectControllerComponent = componentAccessor.getComponent(ref, EffectControllerComponent.getComponentType())) == null) {
            return false;
        }
        return effectControllerComponent.hasEffect(this.entityEffect);
    }

    @Override
    @Nonnull
    public String toString() {
        return "HasEffectCondition{entityEffect=" + String.valueOf(this.entityEffect) + "}";
    }
}

