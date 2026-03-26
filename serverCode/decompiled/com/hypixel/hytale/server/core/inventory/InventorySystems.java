/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemUtility;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemWeapon;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.StatModifiersManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.HotbarManager;
import com.hypixel.hytale.server.core.inventory.InventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InventorySystems {

    public static class PlayerInventoryChangeEventSystem
    extends EntityEventSystem<EntityStore, InventoryChangeEvent> {
        public PlayerInventoryChangeEventSystem() {
            super(InventoryChangeEvent.class);
        }

        @Override
        public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InventoryChangeEvent event) {
            Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());
            assert (playerComponent != null);
            HotbarManager hotbarManager = playerComponent.getHotbarManager();
            if (hotbarManager.getIsCurrentlyLoadingHotbar()) {
                return;
            }
            if (!playerComponent.getGameMode().equals((Object)GameMode.Creative)) {
                return;
            }
            InventoryComponent.Hotbar hotbarComponent = archetypeChunk.getComponent(index, InventoryComponent.Hotbar.getComponentType());
            assert (hotbarComponent != null);
            ItemContainer hotbarInventory = hotbarComponent.getInventory();
            if (!event.getItemContainer().equals(hotbarInventory)) {
                return;
            }
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            hotbarManager.saveHotbar(ref, (short)hotbarManager.getCurrentHotbarIndex(), store);
        }

        @Override
        @Nullable
        public Query<EntityStore> getQuery() {
            return Query.and(Player.getComponentType(), InventoryComponent.Hotbar.getComponentType());
        }
    }

    @Deprecated(forRemoval=true)
    public static class LegacyUtilityChangeStatSystem
    extends EntityTickingSystem<EntityStore> {
        private final Query<EntityStore> query = Query.and(InventoryComponent.Utility.getComponentType(), AllLegacyLivingEntityTypesQuery.INSTANCE);
        private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency(Order.BEFORE, UtilityChangeEventSystem.class));

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            LivingEntity entity = (LivingEntity)EntityUtils.getEntity(index, archetypeChunk);
            assert (entity != null);
            InventoryComponent.Utility inventory = archetypeChunk.getComponent(index, InventoryComponent.Utility.getComponentType());
            assert (inventory != null);
            ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> changeEvents = inventory.getChangeEvents();
            if (changeEvents.isEmpty()) {
                return;
            }
            boolean changed = false;
            byte activeHotbarSlot = inventory.getActiveSlot();
            for (ItemContainer.ItemContainerChangeEvent event : changeEvents) {
                SlotTransaction slot;
                Transaction transaction;
                if (activeHotbarSlot == -1 || !event.transaction().wasSlotModified(activeHotbarSlot) || (transaction = event.transaction()) instanceof SlotTransaction && (slot = (SlotTransaction)transaction).getSlotAfter() != null && ItemStack.isEquivalentType(slot.getSlotBefore(), slot.getSlotAfter())) continue;
                changed = true;
            }
            if (!changed) {
                return;
            }
            entity.invalidateEquipmentNetwork();
            EntityStatMap entityStatMapComponent = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
            if (entityStatMapComponent != null) {
                StatModifiersManager statModifiersManager = entityStatMapComponent.getStatModifiersManager();
                statModifiersManager.scheduleRecalculate();
                ItemStack itemStack = inventory.getActiveItem();
                if (itemStack == null) {
                    return;
                }
                ItemUtility itemUtility = itemStack.getItem().getUtility();
                if (itemUtility == null) {
                    return;
                }
                int[] entityStatsToClear = itemUtility.getEntityStatsToClear();
                if (entityStatsToClear == null) {
                    return;
                }
                statModifiersManager.queueEntityStatsToClear(entityStatsToClear);
            }
        }

        @Override
        @Nullable
        public Query<EntityStore> getQuery() {
            return this.query;
        }

        @Override
        @Nonnull
        public Set<Dependency<EntityStore>> getDependencies() {
            return this.dependencies;
        }
    }

    @Deprecated(forRemoval=true)
    public static class LegacyHotbarChangeStatSystem
    extends EntityTickingSystem<EntityStore> {
        private final Query<EntityStore> query = Query.and(InventoryComponent.Hotbar.getComponentType(), AllLegacyLivingEntityTypesQuery.INSTANCE);
        private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency(Order.BEFORE, HotbarChangeEventSystem.class));

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            LivingEntity entity = (LivingEntity)EntityUtils.getEntity(index, archetypeChunk);
            assert (entity != null);
            InventoryComponent.Hotbar inventory = archetypeChunk.getComponent(index, InventoryComponent.Hotbar.getComponentType());
            assert (inventory != null);
            ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> changeEvents = inventory.getChangeEvents();
            if (changeEvents.isEmpty()) {
                return;
            }
            boolean changed = false;
            byte activeHotbarSlot = inventory.getActiveSlot();
            for (ItemContainer.ItemContainerChangeEvent event : changeEvents) {
                SlotTransaction slot;
                Transaction transaction;
                if (activeHotbarSlot == -1 || !event.transaction().wasSlotModified(activeHotbarSlot) || (transaction = event.transaction()) instanceof SlotTransaction && (slot = (SlotTransaction)transaction).getSlotAfter() != null && ItemStack.isEquivalentType(slot.getSlotBefore(), slot.getSlotAfter())) continue;
                changed = true;
            }
            if (!changed) {
                return;
            }
            entity.invalidateEquipmentNetwork();
            EntityStatMap entityStatMapComponent = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
            if (entityStatMapComponent == null) {
                return;
            }
            StatModifiersManager statModifiersManager = entityStatMapComponent.getStatModifiersManager();
            statModifiersManager.scheduleRecalculate();
            ItemStack itemStack = inventory.getActiveItem();
            if (itemStack == null) {
                return;
            }
            ItemWeapon itemWeapon = itemStack.getItem().getWeapon();
            if (itemWeapon == null) {
                return;
            }
            int[] entityStatsToClear = itemWeapon.getEntityStatsToClear();
            if (entityStatsToClear == null) {
                return;
            }
            statModifiersManager.queueEntityStatsToClear(entityStatsToClear);
        }

        @Override
        @Nullable
        public Query<EntityStore> getQuery() {
            return this.query;
        }

        @Override
        @Nonnull
        public Set<Dependency<EntityStore>> getDependencies() {
            return this.dependencies;
        }
    }

    @Deprecated(forRemoval=true)
    public static class LegacyArmorChangeStatSystem
    extends EntityTickingSystem<EntityStore> {
        private final Query<EntityStore> query = Query.and(InventoryComponent.Armor.getComponentType(), AllLegacyLivingEntityTypesQuery.INSTANCE);
        private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency(Order.BEFORE, ArmorChangeEventSystem.class));

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            LivingEntity entity = (LivingEntity)EntityUtils.getEntity(index, archetypeChunk);
            assert (entity != null);
            InventoryComponent.Armor inventory = archetypeChunk.getComponent(index, InventoryComponent.Armor.getComponentType());
            assert (inventory != null);
            ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> changeEvents = inventory.getChangeEvents();
            if (changeEvents.isEmpty()) {
                return;
            }
            entity.invalidateEquipmentNetwork();
            EntityStatMap entityStatMapComponent = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
            if (entityStatMapComponent != null) {
                entityStatMapComponent.getStatModifiersManager().scheduleRecalculate();
            }
        }

        @Override
        @Nullable
        public Query<EntityStore> getQuery() {
            return this.query;
        }

        @Override
        @Nonnull
        public Set<Dependency<EntityStore>> getDependencies() {
            return this.dependencies;
        }
    }

    public static class ToolChangeEventSystem
    extends InventoryChangeEventSystem<InventoryComponent.Tool> {
        public ToolChangeEventSystem() {
            super(InventoryComponent.Tool.getComponentType());
        }
    }

    public static class BackpackChangeEventSystem
    extends InventoryChangeEventSystem<InventoryComponent.Backpack> {
        public BackpackChangeEventSystem() {
            super(InventoryComponent.Backpack.getComponentType());
        }
    }

    public static class UtilityChangeEventSystem
    extends InventoryChangeEventSystem<InventoryComponent.Utility> {
        public UtilityChangeEventSystem() {
            super(InventoryComponent.Utility.getComponentType());
        }
    }

    public static class HotbarChangeEventSystem
    extends InventoryChangeEventSystem<InventoryComponent.Hotbar> {
        public HotbarChangeEventSystem() {
            super(InventoryComponent.Hotbar.getComponentType());
        }
    }

    public static class ArmorChangeEventSystem
    extends InventoryChangeEventSystem<InventoryComponent.Armor> {
        public ArmorChangeEventSystem() {
            super(InventoryComponent.Armor.getComponentType());
        }
    }

    public static class StorageChangeEventSystem
    extends InventoryChangeEventSystem<InventoryComponent.Storage> {
        public StorageChangeEventSystem() {
            super(InventoryComponent.Storage.getComponentType());
        }
    }

    public static abstract class InventoryChangeEventSystem<Inv extends InventoryComponent>
    extends EntityTickingSystem<EntityStore> {
        protected final ComponentType<EntityStore, Inv> componentType;

        protected InventoryChangeEventSystem(ComponentType<EntityStore, Inv> componentType) {
            this.componentType = componentType;
        }

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            InventoryComponent inventory = (InventoryComponent)archetypeChunk.getComponent(index, this.componentType);
            assert (inventory != null);
            ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> changeEvents = inventory.getChangeEvents();
            ItemContainer.ItemContainerChangeEvent changeEvent = null;
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            while ((changeEvent = changeEvents.poll()) != null) {
                InventoryChangeEvent event = new InventoryChangeEvent(this.componentType, inventory, changeEvent.container(), changeEvent.transaction());
                commandBuffer.invoke(ref, event);
            }
        }

        @Override
        @Nullable
        public Query<EntityStore> getQuery() {
            return this.componentType;
        }
    }
}

