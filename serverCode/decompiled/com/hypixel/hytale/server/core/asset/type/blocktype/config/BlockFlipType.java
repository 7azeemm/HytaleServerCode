/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import javax.annotation.Nonnull;

public enum BlockFlipType {
    ORTHOGONAL,
    ORTHOGONAL_INVERSE,
    SYMMETRIC;


    public Rotation flipYaw(@Nonnull Rotation rotation, Axis axis) {
        return this.flipComponent(rotation, axis, Axis.Y, Axis.Z, rotation.getAxisOfAlignment());
    }

    private Rotation flipComponent(@Nonnull Rotation rotation, Axis axis, Axis ownAxis, Axis negateAxis, Axis alignment) {
        switch (this.ordinal()) {
            case 0: {
                int multiplier;
                int n = multiplier = rotation.getDegrees() % 180 == 90 ? 1 : -1;
                if (axis == negateAxis) {
                    multiplier = -multiplier;
                }
                int index = rotation.ordinal() + multiplier + Rotation.VALUES.length;
                if (axis == ownAxis && rotation.getDegrees() % 180 == 90) {
                    index += 2;
                }
                return Rotation.VALUES[index %= Rotation.VALUES.length];
            }
            case 1: {
                int multiplierInv;
                int n = multiplierInv = rotation.getDegrees() % 180 == 90 ? -1 : 1;
                if (axis == negateAxis) {
                    multiplierInv = -multiplierInv;
                }
                int indexInv = rotation.ordinal() + multiplierInv + Rotation.VALUES.length;
                if (axis == ownAxis && rotation.getDegrees() % 180 == 90) {
                    indexInv += 2;
                }
                return Rotation.VALUES[indexInv %= Rotation.VALUES.length];
            }
            case 2: {
                if (axis == ownAxis || alignment == axis) {
                    return rotation.add(Rotation.OneEighty);
                }
                return rotation;
            }
        }
        throw new UnsupportedOperationException();
    }
}

