/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFlipType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record RotationTuple(int index, Rotation yaw, Rotation pitch, Rotation roll) {
    public static final RotationTuple[] EMPTY_ARRAY = new RotationTuple[0];
    public static final RotationTuple NONE = new RotationTuple(0, Rotation.None, Rotation.None, Rotation.None);
    public static final int NONE_INDEX = 0;
    @Nonnull
    public static final RotationTuple[] VALUES;

    public static RotationTuple of(@Nonnull Rotation yaw, @Nonnull Rotation pitch, @Nonnull Rotation roll) {
        return VALUES[RotationTuple.index(yaw, pitch, roll)];
    }

    public static RotationTuple of(@Nonnull Rotation yaw, @Nonnull Rotation pitch) {
        return VALUES[RotationTuple.index(yaw, pitch, Rotation.None)];
    }

    public static int index(@Nonnull Rotation yaw, @Nonnull Rotation pitch, @Nonnull Rotation roll) {
        return roll.ordinal() * Rotation.VALUES.length * Rotation.VALUES.length + pitch.ordinal() * Rotation.VALUES.length + yaw.ordinal();
    }

    public static RotationTuple get(int index) {
        return VALUES[index];
    }

    public static RotationTuple getRotation(@Nonnull RotationTuple[] rotations, @Nonnull RotationTuple pair, @Nonnull Rotation rotation) {
        int index = 0;
        for (int i = 0; i < rotations.length; ++i) {
            RotationTuple rotationPair = rotations[i];
            if (!pair.equals(rotationPair)) continue;
            index = i;
            break;
        }
        return rotations[(index + rotation.ordinal()) % Rotation.VALUES.length];
    }

    public static RotationTuple flip(@Nonnull RotationTuple blockRotation, @Nullable BlockFlipType flipType, @Nonnull Axis axis, int[][][] flipCorrections) {
        int[][] matrix = RotationTuple.eulerToMatrix(blockRotation.yaw, blockRotation.pitch, blockRotation.roll);
        int flipRow = switch (axis) {
            default -> throw new MatchException(null, null);
            case Axis.X -> 0;
            case Axis.Y -> 1;
            case Axis.Z -> 2;
        };
        for (int i = 0; i < 3; ++i) {
            matrix[flipRow][i] = -matrix[flipRow][i];
        }
        int[][] correction = flipCorrections[flipType.ordinal()];
        int[][] result = RotationTuple.multiply3x3(matrix, correction);
        return RotationTuple.matrixToRotationTuple(result);
    }

    @Nonnull
    public RotationTuple composeOnAxis(@Nonnull Axis axis, @Nonnull Rotation rotation) {
        int[][] current = RotationTuple.eulerToMatrix(this.yaw, this.pitch, this.roll);
        int[][] axisRot = RotationTuple.axisRotationMatrix(axis, rotation);
        int[][] result = RotationTuple.multiply3x3(axisRot, current);
        return RotationTuple.matrixToRotationTuple(result);
    }

    private static int[][] eulerToMatrix(@Nonnull Rotation yaw, @Nonnull Rotation pitch, @Nonnull Rotation roll) {
        int cy = RotationTuple.cos90(yaw);
        int sy = RotationTuple.sin90(yaw);
        int cp = RotationTuple.cos90(pitch);
        int sp = RotationTuple.sin90(pitch);
        int cr = RotationTuple.cos90(roll);
        int sr = RotationTuple.sin90(roll);
        return new int[][]{{cy * cr + sy * sp * sr, -cy * sr + sy * sp * cr, sy * cp}, {cp * sr, cp * cr, -sp}, {-sy * cr + cy * sp * sr, sy * sr + cy * sp * cr, cy * cp}};
    }

    private static int[][] axisRotationMatrix(@Nonnull Axis axis, @Nonnull Rotation rotation) {
        int[][] nArrayArray;
        int c = RotationTuple.cos90(rotation);
        int s = RotationTuple.sin90(rotation);
        switch (axis) {
            default: {
                throw new MatchException(null, null);
            }
            case X: {
                int[][] nArrayArray2 = new int[3][];
                nArrayArray2[0] = new int[]{1, 0, 0};
                nArrayArray2[1] = new int[]{0, c, -s};
                nArrayArray = nArrayArray2;
                nArrayArray2[2] = new int[]{0, s, c};
                break;
            }
            case Y: {
                int[][] nArrayArray3 = new int[3][];
                nArrayArray3[0] = new int[]{c, 0, s};
                nArrayArray3[1] = new int[]{0, 1, 0};
                nArrayArray = nArrayArray3;
                nArrayArray3[2] = new int[]{-s, 0, c};
                break;
            }
            case Z: {
                int[][] nArrayArray4 = new int[3][];
                nArrayArray4[0] = new int[]{c, -s, 0};
                nArrayArray4[1] = new int[]{s, c, 0};
                nArrayArray = nArrayArray4;
                nArrayArray4[2] = new int[]{0, 0, 1};
            }
        }
        return nArrayArray;
    }

    private static int[][] multiply3x3(int[][] a, int[][] b) {
        int[][] r = new int[3][3];
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                r[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
            }
        }
        return r;
    }

    private static RotationTuple matrixToRotationTuple(int[][] m) {
        Rotation newRoll;
        Rotation newYaw;
        int sp = -m[1][2];
        Rotation newPitch = RotationTuple.sinToRotation(sp);
        if (sp != 1 && sp != -1) {
            newYaw = RotationTuple.atan2_90(m[0][2], m[2][2]);
            newRoll = RotationTuple.atan2_90(m[1][0], m[1][1]);
        } else {
            newYaw = RotationTuple.atan2_90(-m[2][0], m[0][0]);
            newRoll = Rotation.None;
        }
        return RotationTuple.of(newYaw, newPitch, newRoll);
    }

    private static int cos90(@Nonnull Rotation r) {
        return switch (r) {
            default -> throw new MatchException(null, null);
            case Rotation.None -> 1;
            case Rotation.Ninety -> 0;
            case Rotation.OneEighty -> -1;
            case Rotation.TwoSeventy -> 0;
        };
    }

    private static int sin90(@Nonnull Rotation r) {
        return switch (r) {
            default -> throw new MatchException(null, null);
            case Rotation.None -> 0;
            case Rotation.Ninety -> 1;
            case Rotation.OneEighty -> 0;
            case Rotation.TwoSeventy -> -1;
        };
    }

    private static Rotation sinToRotation(int s) {
        return switch (s) {
            case 0 -> Rotation.None;
            case 1 -> Rotation.Ninety;
            case -1 -> Rotation.TwoSeventy;
            default -> throw new IllegalArgumentException("Invalid sin value for 90-degree rotation: " + s);
        };
    }

    private static Rotation atan2_90(int sinVal, int cosVal) {
        if (sinVal == 0 && cosVal == 1) {
            return Rotation.None;
        }
        if (sinVal == 1 && cosVal == 0) {
            return Rotation.Ninety;
        }
        if (sinVal == 0 && cosVal == -1) {
            return Rotation.OneEighty;
        }
        if (sinVal == -1 && cosVal == 0) {
            return Rotation.TwoSeventy;
        }
        throw new IllegalArgumentException("Invalid atan2 values for 90-degree rotation: sin=" + sinVal + " cos=" + cosVal);
    }

    @Nonnull
    public RotationTuple add(@Nonnull RotationTuple rotation) {
        return RotationTuple.of(rotation.yaw.add(this.yaw), rotation.pitch.add(this.pitch), rotation.roll.add(this.roll));
    }

    @Nonnull
    public Vector3d rotatedVector(@Nonnull Vector3d vector) {
        return Rotation.rotate(vector, this.yaw, this.pitch, this.roll);
    }

    public void applyRotationTo(@Nonnull Vector3i vector) {
        Rotation.applyRotationTo(vector, this.yaw, this.pitch, this.roll);
    }

    public void applyRotationTo(@Nonnull Vector3f vector) {
        Rotation.applyRotationTo(vector, this.yaw, this.pitch, this.roll);
    }

    public void applyRotationTo(@Nonnull Vector3d vector) {
        Rotation.applyRotationTo(vector, this.yaw, this.pitch, this.roll);
    }

    public void undoRotationTo(@Nonnull Vector3i vector) {
        Rotation.undoRotationTo(vector, this.yaw, this.pitch, this.roll);
    }

    public void undoRotationTo(@Nonnull Vector3f vector) {
        Rotation.undoRotationTo(vector, this.yaw, this.pitch, this.roll);
    }

    public void undoRotationTo(@Nonnull Vector3d vector) {
        Rotation.undoRotationTo(vector, this.yaw, this.pitch, this.roll);
    }

    static {
        RotationTuple[] arr = new RotationTuple[Rotation.VALUES.length * Rotation.VALUES.length * Rotation.VALUES.length];
        arr[0] = NONE;
        for (Rotation roll : Rotation.VALUES) {
            for (Rotation pitch : Rotation.VALUES) {
                for (Rotation yaw : Rotation.VALUES) {
                    if (yaw == Rotation.None && pitch == Rotation.None && roll == Rotation.None) continue;
                    int index = RotationTuple.index(yaw, pitch, roll);
                    arr[index] = new RotationTuple(index, yaw, pitch, roll);
                }
            }
        }
        VALUES = arr;
    }
}

