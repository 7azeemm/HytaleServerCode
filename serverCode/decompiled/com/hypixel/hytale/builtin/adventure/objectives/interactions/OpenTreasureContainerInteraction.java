/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.adventure.objectives.interactions;

import com.hypixel.hytale.builtin.adventure.objectives.blockstates.TreasureChestBlock;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpenTreasureContainerInteraction
extends SimpleBlockInteraction {
    public static final BuilderCodec<OpenTreasureContainerInteraction> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(OpenTreasureContainerInteraction.class, OpenTreasureContainerInteraction::new, SimpleBlockInteraction.CODEC).documentation("Opens the container of the block currently being interacted with.")).build();

    @Override
    protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull Vector3i pos, @Nonnull CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = context.getEntity();
        Store<EntityStore> store = ref.getStore();
        Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
        if (playerComponent == null) {
            return;
        }
        ChunkStore chunkStore = world.getChunkStore();
        Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
        if (chunkRef == null) {
            return;
        }
        BlockComponentChunk blockComponentChunk = chunkStore.getStore().getComponent(chunkRef, BlockComponentChunk.getComponentType());
        if (blockComponentChunk == null) {
            return;
        }
        Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(pos.x, pos.y, pos.z));
        if (blockRef == null) {
            return;
        }
        ItemContainerBlock itemContainerBlock = chunkStore.getStore().getComponent(blockRef, ItemContainerBlock.getComponentType());
        TreasureChestBlock treasureBlock = chunkStore.getStore().getComponent(blockRef, TreasureChestBlock.getComponentType());
        if (itemContainerBlock == null || treasureBlock == null) {
            playerComponent.sendMessage(Message.translation("server.interactions.invalidBlockState").param("interaction", this.getClass().getSimpleName()).param("blockState", chunkStore.getStore().getArchetype(blockRef).toString()));
            return;
        }
        BlockType blockType = world.getBlockType(pos.x, pos.y, pos.z);
        if (!treasureBlock.canOpen(ref, commandBuffer)) {
            return;
        }
        UUIDComponent uuidComponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());
        assert (uuidComponent != null);
        UUID uuid = uuidComponent.getUuid();
        Object chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
        ContainerBlockWindow window = new ContainerBlockWindow(pos.x, pos.y, pos.z, ((WorldChunk)chunk).getRotationIndex(pos.x, pos.y, pos.z), blockType, itemContainerBlock.getItemContainer());
        Map<UUID, ContainerBlockWindow> windows = itemContainerBlock.getWindows();
        if (windows.putIfAbsent(uuid, window) == null) {
            if (playerComponent.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, window)) {
                BlockType interactionState;
                window.registerCloseEvent(event -> {
                    BlockType interactionState;
                    windows.remove(uuid, window);
                    BlockType currentBlockType = world.getBlockType(pos);
                    if (windows.isEmpty()) {
                        world.setBlockInteractionState(pos, currentBlockType, "CloseWindow");
                    }
                    if ((interactionState = currentBlockType.getBlockForState("CloseWindow")) == null) {
                        return;
                    }
                    int soundEventIndex = interactionState.getInteractionSoundEventIndex();
                    if (soundEventIndex == 0) {
                        return;
                    }
                    int rotationIndex = chunk.getRotationIndex(pos.x, pos.y, pos.z);
                    Vector3d soundPos = new Vector3d();
                    blockType.getBlockCenter(rotationIndex, soundPos);
                    soundPos.add(pos);
                    SoundUtil.playSoundEvent3d(ref, soundEventIndex, soundPos, (ComponentAccessor<EntityStore>)commandBuffer);
                });
                if (windows.size() == 1) {
                    world.setBlockInteractionState(pos, blockType, "OpenWindow");
                }
                if ((interactionState = blockType.getBlockForState("OpenWindow")) == null) {
                    return;
                }
                int soundEventIndex = interactionState.getInteractionSoundEventIndex();
                if (soundEventIndex == 0) {
                    return;
                }
                int rotationIndex = ((WorldChunk)chunk).getRotationIndex(pos.x, pos.y, pos.z);
                Vector3d soundPos = new Vector3d();
                blockType.getBlockCenter(rotationIndex, soundPos);
                soundPos.add(pos);
                SoundUtil.playSoundEvent3d(ref, soundEventIndex, soundPos, commandBuffer);
            } else {
                windows.remove(uuid, window);
            }
        }
        treasureBlock.onOpen(ref, world, store);
    }

    @Override
    protected void simulateInteractWithBlock(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock) {
    }
}

