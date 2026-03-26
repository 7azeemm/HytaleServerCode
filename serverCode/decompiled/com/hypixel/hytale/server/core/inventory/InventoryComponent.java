/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.EmptyItemContainer;
import com.hypixel.hytale.server.core.inventory.container.FetchedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainerUtil;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class InventoryComponent
implements Component<EntityStore> {
    public static final byte INACTIVE_SLOT_INDEX = -1;
    public static final short DEFAULT_HOTBAR_CAPACITY = 9;
    public static final short DEFAULT_UTILITY_CAPACITY = 4;
    public static final short DEFAULT_TOOLS_CAPACITY = 23;
    public static final short DEFAULT_ARMOR_CAPACITY = (short)ItemArmorSlot.VALUES.length;
    public static final short DEFAULT_STORAGE_ROWS = 4;
    public static final short DEFAULT_STORAGE_COLUMNS = 9;
    public static final short DEFAULT_STORAGE_CAPACITY = 36;
    public static final int HOTBAR_SECTION_ID = -1;
    public static final int STORAGE_SECTION_ID = -2;
    public static final int ARMOR_SECTION_ID = -3;
    public static final int UTILITY_SECTION_ID = -5;
    public static final int TOOLS_SECTION_ID = -8;
    public static final int BACKPACK_SECTION_ID = -9;
    public static final BuilderCodec<InventoryComponent> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.abstractBuilder(InventoryComponent.class).append(new KeyedCodec<ItemContainer>("Inventory", ItemContainer.CODEC), (o, i) -> {
        o.inventory = i;
    }, o -> o.inventory).add()).afterDecode(InventoryComponent::postDecode)).build();
    protected final AtomicBoolean isDirty = new AtomicBoolean();
    protected final AtomicBoolean needsSaving = new AtomicBoolean();
    protected ItemContainer inventory = EmptyItemContainer.INSTANCE;
    @Nullable
    protected EventRegistration<Void, ItemContainer.ItemContainerChangeEvent> changeEvent = null;
    protected ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> changeEvents = new ConcurrentLinkedQueue();
    public static ComponentType<EntityStore, ? extends InventoryComponent>[] HOTBAR_STORAGE_BACKPACK;
    public static ComponentType<EntityStore, ? extends InventoryComponent>[] HOTBAR_FIRST;
    public static ComponentType<EntityStore, ? extends InventoryComponent>[] STORAGE_FIRST;
    public static ComponentType<EntityStore, ? extends InventoryComponent>[] BACKPACK_STORAGE_HOTBAR;
    public static ComponentType<EntityStore, ? extends InventoryComponent>[] BACKPACK_HOTBAR_STORAGE;
    public static ComponentType<EntityStore, ? extends InventoryComponent>[] STORAGE_HOTBAR_BACKPACK;
    public static ComponentType<EntityStore, ? extends InventoryComponent>[] ARMOR_HOTBAR_UTILITY_STORAGE;
    public static ComponentType<EntityStore, ? extends InventoryComponent>[] HOTBAR_UTILITY_CONSUMABLE_STORAGE;
    public static ComponentType<EntityStore, ? extends InventoryComponent>[] EVERYTHING;

    public InventoryComponent() {
    }

    public InventoryComponent(short capacity) {
        this.inventory = capacity == 0 ? EmptyItemContainer.INSTANCE : new SimpleItemContainer(capacity);
        this.registerChangeEvent();
    }

    public void ensureCapacity(short capacity, @Nonnull List<ItemStack> remainder) {
        if (this.inventory.getCapacity() == capacity) {
            return;
        }
        this.unregisterChangeEvent();
        this.inventory = ItemContainer.ensureContainerCapacity(this.inventory, capacity, SimpleItemContainer::new, remainder);
        this.registerChangeEvent();
    }

    protected void registerChangeEvent() {
        if (this.inventory == EmptyItemContainer.INSTANCE) {
            return;
        }
        this.changeEvent = this.inventory.registerChangeEvent(itemContainerChangeEvent -> {
            this.markChanged();
            this.changeEvents.add((ItemContainer.ItemContainerChangeEvent)itemContainerChangeEvent);
        });
    }

    protected void unregisterChangeEvent() {
        if (this.changeEvent == null) {
            return;
        }
        this.changeEvent.unregister();
        this.changeEvent = null;
    }

    protected void markChanged() {
        this.isDirty.set(true);
        this.needsSaving.set(true);
    }

    public void markDirty() {
        this.isDirty.set(true);
    }

    public boolean consumeIsDirty() {
        return this.isDirty.getAndSet(false);
    }

    public boolean consumeNeedsSaving() {
        return this.needsSaving.getAndSet(false);
    }

    public ItemContainer getInventory() {
        return this.inventory;
    }

    private void postDecode() {
        this.registerChangeEvent();
    }

    public ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> getChangeEvents() {
        return this.changeEvents;
    }

    @Override
    @Nullable
    public abstract Component<EntityStore> clone();

    public static void setupCombined(ComponentType<EntityStore, Storage> storageInventoryComponentType, ComponentType<EntityStore, Armor> armorInventoryComponentType, ComponentType<EntityStore, Hotbar> hotbarInventoryComponentType, ComponentType<EntityStore, Utility> utilityInventoryComponentType, ComponentType<EntityStore, Backpack> backpackInventoryComponentType, ComponentType<EntityStore, Tool> toolInventoryComponentType) {
        HOTBAR_STORAGE_BACKPACK = new ComponentType[]{hotbarInventoryComponentType, storageInventoryComponentType, backpackInventoryComponentType};
        HOTBAR_FIRST = new ComponentType[]{hotbarInventoryComponentType, storageInventoryComponentType};
        STORAGE_FIRST = new ComponentType[]{storageInventoryComponentType, hotbarInventoryComponentType};
        BACKPACK_STORAGE_HOTBAR = new ComponentType[]{backpackInventoryComponentType, storageInventoryComponentType, hotbarInventoryComponentType};
        BACKPACK_HOTBAR_STORAGE = new ComponentType[]{backpackInventoryComponentType, hotbarInventoryComponentType, storageInventoryComponentType};
        STORAGE_HOTBAR_BACKPACK = new ComponentType[]{storageInventoryComponentType, hotbarInventoryComponentType, backpackInventoryComponentType};
        ARMOR_HOTBAR_UTILITY_STORAGE = new ComponentType[]{armorInventoryComponentType, hotbarInventoryComponentType, utilityInventoryComponentType, storageInventoryComponentType};
        HOTBAR_UTILITY_CONSUMABLE_STORAGE = new ComponentType[]{hotbarInventoryComponentType, utilityInventoryComponentType, storageInventoryComponentType};
        EVERYTHING = new ComponentType[]{armorInventoryComponentType, hotbarInventoryComponentType, utilityInventoryComponentType, storageInventoryComponentType, backpackInventoryComponentType};
    }

    @Nullable
    public static ComponentType<EntityStore, ? extends InventoryComponent> getComponentTypeById(int id) {
        if (id >= 0) {
            return null;
        }
        return switch (id) {
            case -1 -> Hotbar.getComponentType();
            case -2 -> Storage.getComponentType();
            case -3 -> Armor.getComponentType();
            case -5 -> Utility.getComponentType();
            case -8 -> Tool.getComponentType();
            case -9 -> Backpack.getComponentType();
            default -> null;
        };
    }

    @Nonnull
    @SafeVarargs
    public static CombinedItemContainer getCombined(@Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull Ref<EntityStore> ref, ComponentType<EntityStore, ? extends InventoryComponent> ... types) {
        CombinedItemContainer inv;
        Combined combined = accessor.getComponent(ref, Combined.getComponentType());
        if (combined == null) {
            combined = new Combined();
            if (accessor instanceof Store) {
                Store store = (Store)accessor;
                if (store.isProcessing()) {
                    Combined finalCombined = combined;
                    ((EntityStore)store.getExternalData()).getWorld().execute(() -> {
                        if (!ref.isValid()) {
                            return;
                        }
                        store.putComponent(ref, Combined.getComponentType(), finalCombined);
                    });
                } else {
                    accessor.putComponent(ref, Combined.getComponentType(), combined);
                }
            } else {
                accessor.putComponent(ref, Combined.getComponentType(), combined);
            }
        }
        if ((inv = combined.inventories.get(types)) != null) {
            return inv;
        }
        int count = 0;
        Archetype<EntityStore> archetype = accessor.getArchetype(ref);
        for (ComponentType<EntityStore, ? extends InventoryComponent> type : types) {
            if (!archetype.contains(type)) continue;
            ++count;
        }
        ItemContainer[] containers = new ItemContainer[count];
        int i = 0;
        for (ComponentType<EntityStore, ? extends InventoryComponent> type : types) {
            InventoryComponent innerInv = accessor.getComponent(ref, type);
            if (innerInv == null) continue;
            int n = i++;
            containers[n] = new FetchedItemContainer(innerInv::getInventory);
        }
        inv = new CombinedItemContainer(containers);
        combined.inventories.put(types, inv);
        return inv;
    }

    @Nonnull
    @SafeVarargs
    public static CombinedItemContainer getCombined(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, int index, ComponentType<EntityStore, ? extends InventoryComponent> ... types) {
        CombinedItemContainer inv;
        Combined combined = archetypeChunk.getComponent(index, Combined.getComponentType());
        if (combined == null) {
            combined = new Combined();
            commandBuffer.putComponent(archetypeChunk.getReferenceTo(index), Combined.getComponentType(), combined);
        }
        if ((inv = combined.inventories.get(types)) != null) {
            return inv;
        }
        int count = 0;
        Archetype<EntityStore> archetype = archetypeChunk.getArchetype();
        for (ComponentType<EntityStore, ? extends InventoryComponent> type : types) {
            if (!archetype.contains(type)) continue;
            ++count;
        }
        ItemContainer[] containers = new ItemContainer[count];
        int i = 0;
        for (ComponentType<EntityStore, ? extends InventoryComponent> type : types) {
            InventoryComponent innerInv = archetypeChunk.getComponent(index, type);
            if (innerInv == null) continue;
            int n = i++;
            containers[n] = new FetchedItemContainer(innerInv::getInventory);
        }
        inv = new CombinedItemContainer(containers);
        combined.inventories.put(types, inv);
        return inv;
    }

    @Nullable
    public static ItemStack getItemInHand(@Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull Ref<EntityStore> ref) {
        Tool toolComponent = accessor.getComponent(ref, Tool.getComponentType());
        if (toolComponent != null && toolComponent.isUsingToolsItem()) {
            return toolComponent.getActiveItem();
        }
        Hotbar hotbarComponent = accessor.getComponent(ref, Hotbar.getComponentType());
        return hotbarComponent != null ? hotbarComponent.getActiveItem() : null;
    }

    public static class Hotbar
    extends InventoryComponent {
        public static final BuilderCodec<Hotbar> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Hotbar.class, Hotbar::new, CODEC).append(new KeyedCodec<Byte>("ActiveSlot", Codec.BYTE), (o, i) -> {
            o.activeSlot = i;
        }, o -> o.activeSlot).add()).afterDecode(Hotbar::afterDecode)).build();
        protected byte activeSlot;

        public static ComponentType<EntityStore, Hotbar> getComponentType() {
            return EntityModule.get().getHotbarInventoryComponentType();
        }

        public Hotbar() {
        }

        public Hotbar(short capacity) {
            super(capacity);
        }

        public Hotbar(ItemContainer hotbar, byte activeHotbarSlot) {
            this.inventory = hotbar;
            this.activeSlot = activeHotbarSlot;
            this.registerChangeEvent();
        }

        @Override
        public void ensureCapacity(short capacity, @Nonnull List<ItemStack> remainder) {
            super.ensureCapacity(capacity, remainder);
            if (this.activeSlot >= this.inventory.getCapacity()) {
                this.activeSlot = (byte)(this.inventory.getCapacity() > 0 ? 0 : -1);
            }
        }

        private void afterDecode() {
            this.activeSlot = this.activeSlot < this.inventory.getCapacity() ? this.activeSlot : (this.inventory.getCapacity() > 0 ? 0 : -1);
        }

        public byte getActiveSlot() {
            return this.activeSlot;
        }

        public void setActiveSlot(byte activeSlot) {
            this.activeSlot = activeSlot;
        }

        @Nullable
        public ItemStack getActiveItem() {
            return this.activeSlot != -1 && this.activeSlot < this.inventory.getCapacity() ? this.inventory.getItemStack(this.activeSlot) : null;
        }

        @Override
        @Nullable
        public Component<EntityStore> clone() {
            Hotbar hotbar = new Hotbar();
            hotbar.inventory = this.inventory.clone();
            hotbar.activeSlot = this.activeSlot;
            return hotbar;
        }
    }

    public static class Storage
    extends InventoryComponent {
        public static final BuilderCodec<Storage> CODEC = BuilderCodec.builder(Storage.class, Storage::new, CODEC).build();

        public static ComponentType<EntityStore, Storage> getComponentType() {
            return EntityModule.get().getStorageInventoryComponentType();
        }

        public Storage() {
        }

        public Storage(short capacity) {
            super(capacity);
        }

        public Storage(ItemContainer storage) {
            this.inventory = storage;
            this.registerChangeEvent();
        }

        @Override
        @Nullable
        public Component<EntityStore> clone() {
            Storage storage = new Storage();
            storage.inventory = this.inventory.clone();
            return storage;
        }
    }

    public static class Armor
    extends InventoryComponent {
        public static final BuilderCodec<Armor> CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(Armor.class, Armor::new, CODEC).afterDecode(Armor::afterDecode)).build();

        public static ComponentType<EntityStore, Armor> getComponentType() {
            return EntityModule.get().getArmorInventoryComponentType();
        }

        public Armor() {
        }

        public Armor(short capacity) {
            super(capacity);
            this.afterDecode();
        }

        public Armor(ItemContainer armor) {
            this.inventory = armor;
            this.registerChangeEvent();
            this.afterDecode();
        }

        private void afterDecode() {
            this.inventory = ItemContainerUtil.trySetArmorFilters(this.inventory);
        }

        @Override
        public void ensureCapacity(short capacity, @Nonnull List<ItemStack> remainder) {
            super.ensureCapacity(capacity, remainder);
            this.inventory = ItemContainerUtil.trySetArmorFilters(this.inventory);
        }

        @Override
        @Nullable
        public Component<EntityStore> clone() {
            Armor armor = new Armor();
            armor.inventory = this.inventory.clone();
            return armor;
        }
    }

    public static class Utility
    extends InventoryComponent {
        public static final BuilderCodec<Utility> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Utility.class, Utility::new, CODEC).append(new KeyedCodec<Byte>("ActiveSlot", Codec.BYTE), (o, i) -> {
            o.activeSlot = i;
        }, o -> o.activeSlot).add()).afterDecode(Utility::afterDecode)).build();
        protected byte activeSlot = (byte)-1;

        public static ComponentType<EntityStore, Utility> getComponentType() {
            return EntityModule.get().getUtilityInventoryComponentType();
        }

        public Utility() {
        }

        public Utility(short capacity) {
            super(capacity);
            this.afterDecode();
        }

        public Utility(ItemContainer utility, byte utilitySlot) {
            this.inventory = utility;
            this.activeSlot = utilitySlot;
            this.registerChangeEvent();
            this.afterDecode();
        }

        @Override
        public void ensureCapacity(short capacity, @Nonnull List<ItemStack> remainder) {
            super.ensureCapacity(capacity, remainder);
            if (this.activeSlot >= this.inventory.getCapacity()) {
                this.activeSlot = (byte)-1;
            }
            this.inventory = ItemContainerUtil.trySetSlotFilters(this.inventory, (type, container, slot, itemStack) -> itemStack == null || itemStack.getItem().getUtility().isUsable());
        }

        private void afterDecode() {
            this.inventory = ItemContainerUtil.trySetSlotFilters(this.inventory, (type, container, slot, itemStack) -> itemStack == null || itemStack.getItem().getUtility().isUsable());
            this.activeSlot = (byte)(this.activeSlot < this.inventory.getCapacity() ? (int)this.activeSlot : -1);
        }

        public byte getActiveSlot() {
            return this.activeSlot;
        }

        public void setActiveSlot(byte activeSlot) {
            this.activeSlot = activeSlot;
        }

        @Nullable
        public ItemStack getActiveItem() {
            return this.activeSlot != -1 && this.activeSlot < this.inventory.getCapacity() ? this.inventory.getItemStack(this.activeSlot) : null;
        }

        @Override
        @Nullable
        public Component<EntityStore> clone() {
            Utility utility = new Utility();
            utility.inventory = this.inventory.clone();
            utility.activeSlot = this.activeSlot;
            return utility;
        }
    }

    public static class Tool
    extends InventoryComponent {
        public static final BuilderCodec<Tool> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Tool.class, Tool::new, CODEC).append(new KeyedCodec<Byte>("ActiveSlot", Codec.BYTE), (o, i) -> {
            o.activeSlot = i;
        }, o -> o.activeSlot).add()).afterDecode(Tool::afterDecode)).build();
        protected byte activeSlot = (byte)-1;
        protected boolean usingToolsItem = false;

        public static ComponentType<EntityStore, Tool> getComponentType() {
            return EntityModule.get().getToolInventoryComponentType();
        }

        public Tool() {
        }

        public Tool(short capacity) {
            super(capacity);
        }

        public Tool(ItemContainer tools, byte toolsSlot) {
            this.inventory = tools;
            this.activeSlot = toolsSlot;
            this.registerChangeEvent();
        }

        @Override
        public void ensureCapacity(short capacity, @Nonnull List<ItemStack> remainder) {
            super.ensureCapacity(capacity, remainder);
            if (this.activeSlot >= this.inventory.getCapacity()) {
                this.activeSlot = (byte)-1;
            }
        }

        private void afterDecode() {
            this.activeSlot = (byte)(this.activeSlot < this.inventory.getCapacity() ? (int)this.activeSlot : -1);
        }

        public byte getActiveSlot() {
            return this.activeSlot;
        }

        public void setActiveSlot(byte activeSlot) {
            this.activeSlot = activeSlot;
        }

        @Nullable
        public ItemStack getActiveItem() {
            return this.activeSlot != -1 && this.activeSlot < this.inventory.getCapacity() ? this.inventory.getItemStack(this.activeSlot) : null;
        }

        public boolean isUsingToolsItem() {
            return this.usingToolsItem;
        }

        public void setUsingToolsItem(boolean usingToolsItem) {
            this.usingToolsItem = usingToolsItem;
        }

        @Override
        @Nullable
        public Component<EntityStore> clone() {
            Tool tool = new Tool();
            tool.inventory = this.inventory.clone();
            tool.activeSlot = this.activeSlot;
            tool.usingToolsItem = this.usingToolsItem;
            return tool;
        }
    }

    public static class Backpack
    extends InventoryComponent {
        public static final BuilderCodec<Backpack> CODEC = BuilderCodec.builder(Backpack.class, Backpack::new, CODEC).build();

        public static ComponentType<EntityStore, Backpack> getComponentType() {
            return EntityModule.get().getBackpackInventoryComponentType();
        }

        public Backpack() {
        }

        public Backpack(short capacity) {
            super(capacity);
        }

        public Backpack(ItemContainer backpack) {
            this.inventory = backpack;
            this.registerChangeEvent();
        }

        public void resize(short capacity, @Nullable List<ItemStack> remainder) {
            this.unregisterChangeEvent();
            this.inventory = capacity > 0 ? ItemContainer.ensureContainerCapacity(this.inventory, capacity, SimpleItemContainer::new, remainder) : ItemContainer.copy(this.inventory, EmptyItemContainer.INSTANCE, remainder);
            this.registerChangeEvent();
            this.markChanged();
        }

        @Override
        @Nullable
        public Component<EntityStore> clone() {
            Backpack backpack = new Backpack();
            backpack.inventory = this.inventory.clone();
            return backpack;
        }
    }

    public static class Combined
    implements Component<EntityStore> {
        private final Object2ObjectOpenCustomHashMap<ComponentType[], CombinedItemContainer> inventories = new Object2ObjectOpenCustomHashMap(new Hash.Strategy<ComponentType[]>(this){

            @Override
            public int hashCode(ComponentType[] o) {
                return Arrays.hashCode(o);
            }

            @Override
            public boolean equals(ComponentType[] a, ComponentType[] b) {
                return Arrays.equals(a, b);
            }
        });

        public static ComponentType<EntityStore, Combined> getComponentType() {
            return EntityModule.get().getCombinedInventoryComponentType();
        }

        @Override
        @Nullable
        public Component<EntityStore> clone() {
            return new Combined();
        }
    }
}

