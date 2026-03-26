/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReplaceCommand
extends AbstractPlayerCommand {
    @Nonnull
    private final RequiredArg<BlockPattern> toArg = this.withRequiredArg("to", "server.commands.replace.toBlock.desc", ArgTypes.BLOCK_PATTERN);

    public ReplaceCommand() {
        super("replace", "server.commands.replace.desc");
        this.setPermissionGroup(GameMode.Creative);
        this.addUsageVariant(new ReplaceFromToCommand());
        this.addSubCommand(new ReplaceSwapCommand());
        this.addSubCommand(new ReplaceRegexCommand());
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        ReplaceCommand.executeBlockReplace(context, store, ref, playerRef, null, (BlockPattern)this.toArg.get(context));
    }

    static void executeBlockReplace(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nullable BlockMask fromMask, @Nonnull BlockPattern toPattern) {
        Player playerComponent = store.getComponent(ref, Player.getComponentType());
        assert (playerComponent != null);
        if (!PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
            return;
        }
        if (fromMask != null && fromMask.hasInvalidBlocks()) {
            context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("key", fromMask.toString()));
            return;
        }
        String toValue = toPattern.toString();
        String fromValue = fromMask != null ? fromMask.toString() : null;
        Material fromMaterial = fromValue != null ? Material.fromKey(fromValue) : null;
        Material toMaterial = Material.fromPattern(toPattern, ThreadLocalRandom.current());
        if (toMaterial.isFluid()) {
            if (fromMaterial == null) {
                context.sendMessage(Message.translation("server.commands.replace.fromRequired"));
                return;
            }
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.replace((Ref<EntityStore>)r, fromMaterial, toMaterial, (ComponentAccessor<EntityStore>)componentAccessor));
            context.sendMessage(Message.translation("server.builderTools.replace.replacementBlockDone").param("from", fromValue).param("to", toValue));
            return;
        }
        if (fromMaterial != null && fromMaterial.isFluid()) {
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.replace((Ref<EntityStore>)r, fromMaterial, toMaterial, (ComponentAccessor<EntityStore>)componentAccessor));
            context.sendMessage(Message.translation("server.builderTools.replace.replacementBlockDone").param("from", fromValue).param("to", toValue));
            return;
        }
        if (fromMask == null) {
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.replace((Ref<EntityStore>)r, null, toPattern, (ComponentAccessor<EntityStore>)componentAccessor));
            context.sendMessage(Message.translation("server.builderTools.replace.replacementAllDone").param("to", toValue));
            return;
        }
        BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.replace((Ref<EntityStore>)r, fromMask, toPattern, (ComponentAccessor<EntityStore>)componentAccessor));
        context.sendMessage(Message.translation("server.builderTools.replace.replacementBlockDone").param("from", fromValue).param("to", toValue));
    }

    private static class ReplaceFromToCommand
    extends AbstractPlayerCommand {
        @Nonnull
        private final RequiredArg<BlockMask> fromArg = this.withRequiredArg("from", "server.commands.replace.from.desc", ArgTypes.BLOCK_MASK);
        @Nonnull
        private final RequiredArg<BlockPattern> toArg = this.withRequiredArg("to", "server.commands.replace.toBlock.desc", ArgTypes.BLOCK_PATTERN);

        public ReplaceFromToCommand() {
            super("server.commands.replace.desc");
            this.setPermissionGroup(GameMode.Creative);
        }

        @Override
        protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            ReplaceCommand.executeBlockReplace(context, store, ref, playerRef, (BlockMask)this.fromArg.get(context), (BlockPattern)this.toArg.get(context));
        }
    }

    private static class ReplaceSwapCommand
    extends AbstractPlayerCommand {
        @Nonnull
        private final RequiredArg<String> fromArg = this.withRequiredArg("from", "server.commands.replace.swap.from.desc", ArgTypes.STRING);
        @Nonnull
        private final RequiredArg<String> toArg = this.withRequiredArg("to", "server.commands.replace.swap.to.desc", ArgTypes.STRING);

        public ReplaceSwapCommand() {
            super("swap", "server.commands.replace.swap.desc");
            this.setPermissionGroup(GameMode.Creative);
        }

        @Override
        protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            Player playerComponent = store.getComponent(ref, Player.getComponentType());
            assert (playerComponent != null);
            if (!PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
                return;
            }
            String fromValue = (String)this.fromArg.get(context);
            String toValue = (String)this.toArg.get(context);
            BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
            String[] blockKeys = fromValue.split(",");
            Int2IntArrayMap swapMap = new Int2IntArrayMap();
            block0: for (int blockId = 0; blockId < assetMap.getAssetCount(); ++blockId) {
                BlockType blockType = assetMap.getAsset(blockId);
                String blockKeyStr = blockType.getId();
                for (String from : blockKeys) {
                    String replacedKey;
                    int index;
                    String fromLower;
                    String trimmedFrom = from.trim();
                    String blockKeyLower = blockKeyStr.toLowerCase();
                    int matchIdx = blockKeyLower.indexOf(fromLower = trimmedFrom.toLowerCase());
                    if (matchIdx == -1 || (index = assetMap.getIndex(replacedKey = blockKeyStr.substring(0, matchIdx) + toValue + blockKeyStr.substring(matchIdx + trimmedFrom.length()))) == Integer.MIN_VALUE) continue;
                    swapMap.put(blockId, index);
                    continue block0;
                }
            }
            if (!swapMap.isEmpty()) {
                BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
                    int replaced = s.replace((Ref<EntityStore>)r, value -> swapMap.getOrDefault(value, value), (ComponentAccessor<EntityStore>)componentAccessor);
                    context.sendMessage(Message.translation("server.builderTools.replace.replacementDone").param("nb", replaced).param("to", toValue));
                });
            } else {
                context.sendMessage(Message.translation("server.commands.replace.noMatchingBlocks").param("blockType", fromValue));
            }
        }
    }

    private static class ReplaceRegexCommand
    extends AbstractPlayerCommand {
        @Nonnull
        private final RequiredArg<String> fromArg = this.withRequiredArg("from", "server.commands.replace.regex.from.desc", ArgTypes.STRING);
        @Nonnull
        private final RequiredArg<BlockPattern> toArg = this.withRequiredArg("to", "server.commands.replace.regex.to.desc", ArgTypes.BLOCK_PATTERN);

        public ReplaceRegexCommand() {
            super("regex", "server.commands.replace.regex.desc");
            this.setPermissionGroup(GameMode.Creative);
        }

        @Override
        protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            Pattern pattern;
            Player playerComponent = store.getComponent(ref, Player.getComponentType());
            assert (playerComponent != null);
            if (!PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
                return;
            }
            String fromValue = (String)this.fromArg.get(context);
            BlockPattern toPattern = (BlockPattern)this.toArg.get(context);
            try {
                pattern = Pattern.compile(fromValue);
            }
            catch (PatternSyntaxException e) {
                context.sendMessage(Message.translation("server.commands.replace.invalidRegex").param("error", e.getMessage()));
                return;
            }
            BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
            ArrayList<String> matchedNames = new ArrayList<String>();
            for (int blockId = 0; blockId < assetMap.getAssetCount(); ++blockId) {
                BlockType blockType = assetMap.getAsset(blockId);
                if (blockType == null || !pattern.matcher(blockType.getId()).matches()) continue;
                matchedNames.add(blockType.getId());
            }
            if (matchedNames.isEmpty()) {
                context.sendMessage(Message.translation("server.commands.replace.noMatchingBlocks").param("blockType", fromValue));
                return;
            }
            BlockMask regexMask = BlockMask.parse(matchedNames.toArray(new String[0]));
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
                s.replace((Ref<EntityStore>)r, regexMask, toPattern, (ComponentAccessor<EntityStore>)componentAccessor);
                context.sendMessage(Message.translation("server.commands.replace.success").param("regex", fromValue).param("replacement", toPattern.toString()));
            });
        }
    }
}

