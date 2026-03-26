/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.modules.debug.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.debug.commands.DebugShapeArrowCommand;
import com.hypixel.hytale.server.core.modules.debug.commands.DebugShapeClearCommand;
import com.hypixel.hytale.server.core.modules.debug.commands.DebugShapeConeCommand;
import com.hypixel.hytale.server.core.modules.debug.commands.DebugShapeCubeCommand;
import com.hypixel.hytale.server.core.modules.debug.commands.DebugShapeCylinderCommand;
import com.hypixel.hytale.server.core.modules.debug.commands.DebugShapeShowForceCommand;
import com.hypixel.hytale.server.core.modules.debug.commands.DebugShapeSphereCommand;
import javax.annotation.Nonnull;

public class DebugShapeSubCommand
extends AbstractCommandCollection {
    public DebugShapeSubCommand() {
        super("shape", "server.commands.debug.shape.desc");
        this.addSubCommand(new DebugShapeSphereCommand());
        this.addSubCommand(new DebugShapeCubeCommand());
        this.addSubCommand(new DebugShapeCylinderCommand());
        this.addSubCommand(new DebugShapeConeCommand());
        this.addSubCommand(new DebugShapeArrowCommand());
        this.addSubCommand(new DebugShapeShowForceCommand());
        this.addSubCommand(new DebugShapeClearCommand());
    }

    static int buildFlags(@Nonnull CommandContext context, @Nonnull FlagArg fadeFlag, @Nonnull FlagArg noWireframeFlag, @Nonnull FlagArg noSolidFlag) {
        int flags = 0;
        if (context.get(fadeFlag).booleanValue()) {
            flags |= DebugUtils.FLAG_FADE;
        }
        if (context.get(noWireframeFlag).booleanValue()) {
            flags |= DebugUtils.FLAG_NO_WIREFRAME;
        }
        if (context.get(noSolidFlag).booleanValue()) {
            flags |= DebugUtils.FLAG_NO_SOLID;
        }
        return flags;
    }
}

