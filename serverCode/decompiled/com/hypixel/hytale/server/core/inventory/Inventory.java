/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.InteractionChainData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.protocol.PickupLocation;
import com.hypixel.hytale.protocol.SmartMoveType;
import com.hypixel.hytale.protocol.packets.inventory.UpdatePlayerInventory;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemUtility;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemWeapon;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.StatModifiersManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ItemContainerWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SortType;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ChangeActiveSlotInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated(forRemoval=true)
public class Inventory {
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
    public static final byte INACTIVE_SLOT_INDEX = -1;
    public static final int VERSION = 4;
    public static final BuilderCodec<Inventory> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Inventory.class, Inventory::new).versioned()).codecVersion(4)).append(new KeyedCodec<ItemContainer>("Storage", ItemContainer.CODEC), (o, i) -> {
        o.deserializedStorage = i;
    }, o -> o.deserializedStorage).add()).append(new KeyedCodec<ItemContainer>("Armor", ItemContainer.CODEC), (o, i) -> {
        o.deserializedArmor = i;
    }, o -> o.deserializedArmor).add()).append(new KeyedCodec<ItemContainer>("HotBar", ItemContainer.CODEC), (o, i) -> {
        o.deserializedHotbar = i;
    }, o -> o.deserializedHotbar).add()).append(new KeyedCodec<ItemContainer>("Utility", ItemContainer.CODEC), (o, i) -> {
        o.deserializedUtility = i;
    }, o -> o.deserializedUtility).add()).append(new KeyedCodec<ItemContainer>("Backpack", ItemContainer.CODEC), (o, i) -> {
        o.deserializedBackpack = i;
    }, o -> o.deserializedBackpack).add()).append(new KeyedCodec<Byte>("ActiveHotbarSlot", Codec.BYTE), (o, i) -> {
        o.deserializedActiveHotbarSlot = i;
    }, o -> o.deserializedActiveHotbarSlot).add()).append(new KeyedCodec<ItemContainer>("Tool", ItemContainer.CODEC), (o, i) -> {
        o.deserializedTools = i;
    }, o -> o.deserializedTools).add()).append(new KeyedCodec<Byte>("ActiveToolsSlot", Codec.BYTE), (o, i) -> {
        o.deserializedActiveToolsSlot = i;
    }, o -> o.deserializedActiveToolsSlot).add()).append(new KeyedCodec<Byte>("ActiveUtilitySlot", Codec.BYTE), (o, i) -> {
        o.deserializedActiveUtilitySlot = i;
    }, o -> o.deserializedActiveUtilitySlot).add()).build();
    private ItemContainer deserializedStorage;
    private ItemContainer deserializedArmor;
    private ItemContainer deserializedHotbar;
    private ItemContainer deserializedUtility;
    private ItemContainer deserializedTools;
    private ItemContainer deserializedBackpack;
    private byte deserializedActiveHotbarSlot;
    private byte deserializedActiveUtilitySlot = (byte)-1;
    private byte deserializedActiveToolsSlot = (byte)-1;
    @Nullable
    private InventoryComponent.Storage storage;
    @Nullable
    private InventoryComponent.Armor armor;
    @Nullable
    private InventoryComponent.Hotbar hotbar;
    @Nullable
    private InventoryComponent.Utility utility;
    @Nullable
    private InventoryComponent.Tool tools;
    @Nullable
    private InventoryComponent.Backpack backpack;
    @Nullable
    private LivingEntity entity;

    public void unregister() {
        this.entity = null;
    }

    public void moveItem(int fromSectionId, int fromSlotId, int quantity, int toSectionId, int toSlotId) {
        ItemContainer fromContainer = this.getSectionById(fromSectionId);
        if (fromContainer == null) {
            return;
        }
        ItemContainer toContainer = this.getSectionById(toSectionId);
        if (toContainer == null) {
            return;
        }
        if (this.entity instanceof Player && this.hotbar != null && (toSectionId == -1 && this.hotbar.getActiveSlot() == toSlotId || fromSectionId == -1 && this.hotbar.getActiveSlot() == fromSlotId)) {
            ItemStack currentItem;
            ItemStack fromItem = fromContainer.getItemStack((short)fromSlotId);
            if (ItemStack.isStackableWith(fromItem, currentItem = toContainer.getItemStack((short)toSlotId)) || ItemStack.isSameItemType(fromItem, currentItem)) {
                fromContainer.moveItemStackFromSlotToSlot((short)fromSlotId, quantity, toContainer, (short)toSlotId);
                return;
            }
            int interactionSlot = toSectionId == -1 && this.hotbar.getActiveSlot() == toSlotId ? toSlotId : this.hotbar.getActiveSlot();
            Ref<EntityStore> ref = this.entity.getReference();
            Store<EntityStore> store = ref.getStore();
            InteractionManager interactionManagerComponent = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
            if (interactionManagerComponent != null) {
                PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
                assert (playerRefComponent != null);
                InteractionContext context = InteractionContext.forInteraction(interactionManagerComponent, ref, InteractionType.SwapFrom, store);
                context.getMetaStore().putMetaObject(Interaction.TARGET_SLOT, interactionSlot);
                context.getMetaStore().putMetaObject(ChangeActiveSlotInteraction.PLACE_MOVED_ITEM, () -> {
                    fromContainer.moveItemStackFromSlotToSlot((short)fromSlotId, quantity, toContainer, (short)toSlotId);
                    if (!ref.isValid()) {
                        return;
                    }
                    InventoryComponent.Storage storage = store.getComponent(ref, InventoryComponent.Storage.getComponentType());
                    InventoryComponent.Armor armor = store.getComponent(ref, InventoryComponent.Armor.getComponentType());
                    InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
                    InventoryComponent.Utility utility = store.getComponent(ref, InventoryComponent.Utility.getComponentType());
                    InventoryComponent.Tool tool = store.getComponent(ref, InventoryComponent.Tool.getComponentType());
                    InventoryComponent.Backpack backpack = store.getComponent(ref, InventoryComponent.Backpack.getComponentType());
                    playerRefComponent.getPacketHandler().writeNoCache(new UpdatePlayerInventory(storage != null ? storage.getInventory().toPacket() : null, armor != null ? armor.getInventory().toPacket() : null, hotbar != null ? hotbar.getInventory().toPacket() : null, utility != null ? utility.getInventory().toPacket() : null, tool != null ? tool.getInventory().toPacket() : null, backpack != null ? backpack.getInventory().toPacket() : null));
                });
                String interactions = context.getRootInteractionId(InteractionType.SwapFrom);
                InteractionChainData data = new InteractionChainData(-1, UUIDUtil.EMPTY_UUID, null, null, null, -interactionSlot - 1, null);
                InteractionChain chain = interactionManagerComponent.initChain(data, InteractionType.SwapFrom, context, RootInteraction.getRootInteractionOrUnknown(interactions), null, false);
                interactionManagerComponent.queueExecuteChain(chain);
                return;
            }
        }
        fromContainer.moveItemStackFromSlotToSlot((short)fromSlotId, quantity, toContainer, (short)toSlotId);
    }

    public void smartMoveItem(@Nonnull Ref<EntityStore> ref, int fromSectionId, int fromSlotId, int quantity, @Nonnull SmartMoveType moveType, PlayerSettings settings, @Nonnull ComponentAccessor<EntityStore> accessor) {
        ItemContainer targetContainer = this.getSectionById(fromSectionId);
        if (targetContainer == null) {
            return;
        }
        ItemStack itemStack = targetContainer.getItemStack((short)fromSlotId);
        if (ItemStack.isEmpty(itemStack)) {
            return;
        }
        block0 : switch (moveType) {
            case EquipOrMergeStack: {
                if (this.tryEquipArmorPart(itemStack, fromSectionId, (short)fromSlotId, quantity, targetContainer, true)) {
                    return;
                }
                LivingEntity livingEntity = this.entity;
                if (livingEntity instanceof Player) {
                    Player player = (Player)livingEntity;
                    for (Window window : player.getWindowManager().getWindows()) {
                        if (!(window instanceof ItemContainerWindow)) continue;
                        ItemContainerWindow itemContainerWindow = (ItemContainerWindow)((Object)window);
                        itemContainerWindow.getItemContainer().combineItemStacksIntoSlot(targetContainer, (short)fromSlotId);
                    }
                }
                CombinedItemContainer everythingInventoryComponent = InventoryComponent.getCombined(accessor, ref, InventoryComponent.EVERYTHING);
                everythingInventoryComponent.combineItemStacksIntoSlot(targetContainer, (short)fromSlotId);
                break;
            }
            case PutInHotbarOrWindow: {
                if (fromSectionId >= 0) {
                    this.moveItemFromCheckToInventory(itemStack, targetContainer, (short)fromSlotId, quantity, settings);
                    return;
                }
                if (this.entity instanceof Player) {
                    for (Window window : ((Player)this.entity).getWindowManager().getWindows()) {
                        ItemContainer itemContainer;
                        MoveTransaction<ItemStackTransaction> transaction;
                        ItemStack remainder;
                        if (!(window instanceof ItemContainerWindow) || !ItemStack.isEmpty(remainder = (transaction = targetContainer.moveItemStackFromSlot((short)fromSlotId, quantity, itemContainer = ((ItemContainerWindow)((Object)window)).getItemContainer())).getAddTransaction().getRemainder()) && remainder.getQuantity() == quantity) continue;
                        return;
                    }
                }
                if (this.tryEquipArmorPart(itemStack, fromSectionId, (short)fromSlotId, quantity, targetContainer, false)) {
                    return;
                }
                switch (fromSectionId) {
                    case -1: {
                        targetContainer.moveItemStackFromSlot((short)fromSlotId, quantity, this.storage.getInventory());
                        break block0;
                    }
                    case -2: {
                        targetContainer.moveItemStackFromSlot((short)fromSlotId, quantity, this.hotbar.getInventory());
                        break block0;
                    }
                }
                this.moveItemFromCheckToInventory(itemStack, targetContainer, (short)fromSlotId, quantity, settings);
                break;
            }
            case PutInHotbarOrBackpack: {
                if (fromSectionId == -9) {
                    this.moveItemFromCheckToInventory(itemStack, targetContainer, (short)fromSlotId, quantity, settings);
                    break;
                }
                targetContainer.moveItemStackFromSlot((short)fromSlotId, quantity, this.getContainerForItemPickup(itemStack.getItem(), settings, PickupLocation.Backpack));
            }
        }
    }

    private boolean tryEquipArmorPart(@Nonnull ItemStack itemStack, int fromSectionId, short fromSlotId, int quantity, ItemContainer targetContainer, boolean forceEquip) {
        Item item = itemStack.getItem();
        ItemArmor itemArmor = item.getArmor();
        if (itemArmor != null && fromSectionId != -3 && (forceEquip || this.armor.getInventory().getItemStack((short)itemArmor.getArmorSlot().ordinal()) == null)) {
            targetContainer.moveItemStackFromSlotToSlot(fromSlotId, quantity, this.armor.getInventory(), (short)itemArmor.getArmorSlot().ordinal());
            return true;
        }
        return false;
    }

    private MoveTransaction<ItemStackTransaction> moveItemFromCheckToInventory(@Nonnull ItemStack itemStack, @Nonnull ItemContainer targetContainer, short fromSlotId, int quantity, PlayerSettings settings) {
        return targetContainer.moveItemStackFromSlot(fromSlotId, quantity, this.getContainerForItemPickup(itemStack.getItem(), settings));
    }

    @Nullable
    public ListTransaction<MoveTransaction<ItemStackTransaction>> takeAll(int inventorySectionId, PlayerSettings settings) {
        ItemContainer container = this.getSectionById(inventorySectionId);
        if (container == null) {
            return null;
        }
        return this.takeAllWithPriority(container, settings);
    }

    public ListTransaction<MoveTransaction<ItemStackTransaction>> takeAllWithPriority(ItemContainer fromContainer, PlayerSettings settings) {
        ObjectArrayList transactions = new ObjectArrayList();
        for (int slot = 0; slot < fromContainer.getCapacity(); ++slot) {
            ItemStack stack = fromContainer.getItemStack((short)slot);
            if (ItemStack.isEmpty(stack)) continue;
            transactions.add(this.moveItemFromCheckToInventory(stack, fromContainer, (short)slot, stack.getQuantity(), settings));
        }
        return new ListTransaction<MoveTransaction<ItemStackTransaction>>(true, transactions);
    }

    @Nullable
    public ListTransaction<MoveTransaction<ItemStackTransaction>> putAll(int inventorySectionId) {
        ItemContainer sectionById = this.getSectionById(inventorySectionId);
        if (sectionById != null) {
            return this.storage.getInventory().moveAllItemStacksTo(sectionById);
        }
        return null;
    }

    @Nullable
    public ListTransaction<MoveTransaction<ItemStackTransaction>> quickStack(int inventorySectionId) {
        ItemContainer sectionById = this.getSectionById(inventorySectionId);
        if (sectionById != null) {
            return this.getCombinedHotbarFirst().quickStackTo(sectionById);
        }
        return null;
    }

    @Nonnull
    public List<ItemStack> dropAllItemStacks() {
        ObjectArrayList<ItemStack> items = new ObjectArrayList<ItemStack>();
        if (this.storage != null) {
            items.addAll(this.storage.getInventory().dropAllItemStacks());
        }
        if (this.armor != null) {
            items.addAll(this.armor.getInventory().dropAllItemStacks());
        }
        if (this.hotbar != null) {
            items.addAll(this.hotbar.getInventory().dropAllItemStacks());
        }
        if (this.utility != null) {
            items.addAll(this.utility.getInventory().dropAllItemStacks());
        }
        if (this.backpack != null) {
            items.addAll(this.backpack.getInventory().dropAllItemStacks());
        }
        return items;
    }

    public void clear() {
        if (this.storage != null) {
            this.storage.getInventory().clear();
        }
        if (this.armor != null) {
            this.armor.getInventory().clear();
        }
        if (this.hotbar != null) {
            this.hotbar.getInventory().clear();
        }
        if (this.utility != null) {
            this.utility.getInventory().clear();
        }
        if (this.backpack != null) {
            this.backpack.getInventory().clear();
        }
    }

    @Nullable
    public ItemContainer getStorage() {
        return this.storage != null ? this.storage.getInventory() : null;
    }

    @Nullable
    public ItemContainer getArmor() {
        return this.armor != null ? this.armor.getInventory() : null;
    }

    @Nullable
    public ItemContainer getHotbar() {
        return this.hotbar != null ? this.hotbar.getInventory() : null;
    }

    @Nullable
    public ItemContainer getUtility() {
        return this.utility != null ? this.utility.getInventory() : null;
    }

    @Nullable
    public ItemContainer getTools() {
        return this.tools != null ? this.tools.getInventory() : null;
    }

    @Nullable
    public ItemContainer getBackpack() {
        return this.backpack != null ? this.backpack.getInventory() : null;
    }

    public CombinedItemContainer getCombinedHotbarFirst() {
        if (this.entity == null) {
            return null;
        }
        Ref<EntityStore> ref = this.entity.getReference();
        if (ref == null || !ref.isValid()) {
            return null;
        }
        return InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.HOTBAR_FIRST);
    }

    public CombinedItemContainer getCombinedStorageFirst() {
        if (this.entity == null) {
            return null;
        }
        Ref<EntityStore> ref = this.entity.getReference();
        if (ref == null || !ref.isValid()) {
            return null;
        }
        return InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.STORAGE_FIRST);
    }

    public CombinedItemContainer getCombinedBackpackStorageHotbar() {
        if (this.entity == null) {
            return null;
        }
        Ref<EntityStore> ref = this.entity.getReference();
        if (ref == null || !ref.isValid()) {
            return null;
        }
        return InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.BACKPACK_STORAGE_HOTBAR);
    }

    public CombinedItemContainer getCombinedBackpackStorageHotbarFirst() {
        if (this.entity == null) {
            return null;
        }
        Ref<EntityStore> ref = this.entity.getReference();
        if (ref == null || !ref.isValid()) {
            return null;
        }
        return InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.HOTBAR_STORAGE_BACKPACK);
    }

    public CombinedItemContainer getCombinedArmorHotbarUtilityStorage() {
        if (this.entity == null) {
            return null;
        }
        Ref<EntityStore> ref = this.entity.getReference();
        if (ref == null || !ref.isValid()) {
            return null;
        }
        return InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.ARMOR_HOTBAR_UTILITY_STORAGE);
    }

    public CombinedItemContainer getCombinedHotbarUtilityConsumableStorage() {
        if (this.entity == null) {
            return null;
        }
        Ref<EntityStore> ref = this.entity.getReference();
        if (ref == null || !ref.isValid()) {
            return null;
        }
        return InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.HOTBAR_UTILITY_CONSUMABLE_STORAGE);
    }

    public CombinedItemContainer getCombinedStorageHotbarBackpack() {
        if (this.entity == null) {
            return null;
        }
        Ref<EntityStore> ref = this.entity.getReference();
        if (ref == null || !ref.isValid()) {
            return null;
        }
        return InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.STORAGE_HOTBAR_BACKPACK);
    }

    private ItemContainer getItemContainerForPickupLocation(@Nonnull PickupLocation pickupLocation) {
        return switch (pickupLocation) {
            case PickupLocation.Hotbar -> this.getCombinedBackpackStorageHotbarFirst();
            case PickupLocation.Storage -> this.getCombinedStorageHotbarBackpack();
            case PickupLocation.Backpack -> this.getCombinedBackpackStorageHotbar();
            default -> this.getCombinedBackpackStorageHotbarFirst();
        };
    }

    @Nonnull
    public ItemContainer getContainerForItemPickup(@Nonnull Item item, PlayerSettings playerSettings) {
        return this.getContainerForItemPickup(item, playerSettings, null);
    }

    @Nonnull
    public ItemContainer getContainerForItemPickup(@Nonnull Item item, PlayerSettings playerSettings, @Nullable PickupLocation overridePickupLocation) {
        BlockType blockType;
        if (overridePickupLocation != null) {
            return this.getItemContainerForPickupLocation(overridePickupLocation);
        }
        if (item.getArmor() != null) {
            return this.getItemContainerForPickupLocation(playerSettings.armorItemsPreferredPickupLocation());
        }
        if (item.getWeapon() != null || item.getTool() != null) {
            return this.getItemContainerForPickupLocation(playerSettings.weaponAndToolItemsPreferredPickupLocation());
        }
        if (item.getUtility().isUsable()) {
            return this.getItemContainerForPickupLocation(playerSettings.usableItemsItemsPreferredPickupLocation());
        }
        BlockType blockType2 = blockType = item.hasBlockType() ? (BlockType)BlockType.getAssetMap().getAsset(item.getBlockId()) : BlockType.EMPTY;
        if (blockType == null) {
            blockType = BlockType.EMPTY;
        }
        if (blockType.getMaterial() == BlockMaterial.Solid) {
            return this.getItemContainerForPickupLocation(playerSettings.solidBlockItemsPreferredPickupLocation());
        }
        return this.getItemContainerForPickupLocation(playerSettings.miscItemsPreferredPickupLocation());
    }

    public void setActiveSlot(@Nonnull Ref<EntityStore> ref, int inventorySectionId, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        int[] entityStatsToClear = null;
        switch (inventorySectionId) {
            case -1: {
                ItemWeapon weapon;
                this.hotbar.setActiveSlot(slot);
                ItemStack itemStack = this.getItemInHand();
                if (itemStack == null || (weapon = itemStack.getItem().getWeapon()) == null) break;
                entityStatsToClear = weapon.getEntityStatsToClear();
                break;
            }
            case -5: {
                this.utility.setActiveSlot(slot);
                ItemStack itemStack = this.getUtilityItem();
                if (itemStack == null) break;
                ItemUtility utility = itemStack.getItem().getUtility();
                entityStatsToClear = utility.getEntityStatsToClear();
                break;
            }
            case -8: {
                this.tools.setActiveSlot(slot);
                break;
            }
            default: {
                throw new IllegalArgumentException("Inventory section with id " + inventorySectionId + " cannot select an active slot");
            }
        }
        this.entity.invalidateEquipmentNetwork();
        EntityStatMap entityStatMapComponent = componentAccessor.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMapComponent != null) {
            StatModifiersManager statModifiersManager = entityStatMapComponent.getStatModifiersManager();
            statModifiersManager.scheduleRecalculate();
            if (entityStatsToClear != null) {
                statModifiersManager.queueEntityStatsToClear(entityStatsToClear);
            }
        }
    }

    public void setActiveSlot(@Nonnull Holder<EntityStore> holder, int inventorySectionId, byte slot) {
        int[] entityStatsToClear = null;
        switch (inventorySectionId) {
            case -1: {
                ItemWeapon weapon;
                this.hotbar.setActiveSlot(slot);
                ItemStack itemStack = this.getItemInHand();
                if (itemStack == null || (weapon = itemStack.getItem().getWeapon()) == null) break;
                entityStatsToClear = weapon.getEntityStatsToClear();
                break;
            }
            case -5: {
                this.utility.setActiveSlot(slot);
                ItemStack itemStack = this.getUtilityItem();
                if (itemStack == null) break;
                ItemUtility utility = itemStack.getItem().getUtility();
                entityStatsToClear = utility.getEntityStatsToClear();
                break;
            }
            case -8: {
                this.tools.setActiveSlot(slot);
                break;
            }
            default: {
                throw new IllegalArgumentException("Inventory section with id " + inventorySectionId + " cannot select an active slot");
            }
        }
        this.entity.invalidateEquipmentNetwork();
        EntityStatMap entityStatMapComponent = holder.getComponent(EntityStatMap.getComponentType());
        if (entityStatMapComponent != null) {
            StatModifiersManager statModifiersManager = entityStatMapComponent.getStatModifiersManager();
            statModifiersManager.scheduleRecalculate();
            if (entityStatsToClear != null) {
                statModifiersManager.queueEntityStatsToClear(entityStatsToClear);
            }
        }
    }

    public byte getActiveSlot(int inventorySectionId) {
        return switch (inventorySectionId) {
            case -1 -> this.hotbar.getActiveSlot();
            case -5 -> this.utility.getActiveSlot();
            case -8 -> this.tools.getActiveSlot();
            default -> throw new IllegalArgumentException("Inventory section with id " + inventorySectionId + " cannot select an active slot");
        };
    }

    public byte getActiveHotbarSlot() {
        return this.hotbar.getActiveSlot();
    }

    public void setActiveHotbarSlot(@Nonnull Ref<EntityStore> ref, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        this.setUsingToolsItem(false);
        this.setActiveSlot(ref, -1, slot, componentAccessor);
    }

    @Nullable
    public ItemStack getActiveHotbarItem() {
        return this.hotbar.getActiveItem();
    }

    @Nullable
    public ItemStack getActiveToolItem() {
        return this.tools.getActiveItem();
    }

    @Nullable
    public ItemStack getItemInHand() {
        return this.tools != null && this.tools.usingToolsItem ? this.getActiveToolItem() : this.getActiveHotbarItem();
    }

    public byte getActiveUtilitySlot() {
        return this.utility.getActiveSlot();
    }

    public void setActiveUtilitySlot(@Nonnull Ref<EntityStore> ref, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        this.setActiveSlot(ref, -5, slot, componentAccessor);
    }

    public void setActiveUtilitySlot(@Nonnull Holder<EntityStore> holder, byte slot) {
        this.setActiveSlot(holder, -5, slot);
    }

    @Nullable
    public ItemStack getUtilityItem() {
        return this.utility.getActiveItem();
    }

    public byte getActiveToolsSlot() {
        return this.tools.getActiveSlot();
    }

    public void setActiveToolsSlot(@Nonnull Ref<EntityStore> ref, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        this.setUsingToolsItem(true);
        this.setActiveSlot(ref, -8, slot, componentAccessor);
    }

    @Nullable
    public ItemStack getToolsItem() {
        return this.tools != null ? this.tools.getActiveItem() : null;
    }

    @Nullable
    public ItemContainer getSectionById(int id) {
        if (id >= 0) {
            Window window;
            if (this.entity instanceof Player && (window = ((Player)this.entity).getWindowManager().getWindow(id)) instanceof ItemContainerWindow) {
                return ((ItemContainerWindow)((Object)window)).getItemContainer();
            }
            return null;
        }
        return switch (id) {
            case -1 -> this.hotbar.getInventory();
            case -2 -> this.storage.getInventory();
            case -3 -> this.armor.getInventory();
            case -5 -> this.utility.getInventory();
            case -8 -> this.tools.getInventory();
            case -9 -> this.backpack.getInventory();
            default -> null;
        };
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public void sortStorage() {
        this.storage.getInventory().sortItems(SortType.TYPE);
        this.storage.markChanged();
    }

    public static boolean containsBrokenItem(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
        CombinedItemContainer everythingInventoryComponent = InventoryComponent.getCombined(accessor, ref, InventoryComponent.EVERYTHING);
        for (short i = 0; i < everythingInventoryComponent.getCapacity(); i = (short)(i + 1)) {
            ItemStack itemStack = everythingInventoryComponent.getItemStack(i);
            if (ItemStack.isEmpty(itemStack) || !itemStack.isBroken()) continue;
            return true;
        }
        return false;
    }

    public void migrateToComponents(Holder<EntityStore> holder) {
        if (this.deserializedStorage != null) {
            holder.putComponent(InventoryComponent.Storage.getComponentType(), new InventoryComponent.Storage(this.deserializedStorage));
            this.deserializedStorage = null;
        }
        if (this.deserializedArmor != null) {
            holder.putComponent(InventoryComponent.Armor.getComponentType(), new InventoryComponent.Armor(this.deserializedArmor));
            this.deserializedArmor = null;
        }
        if (this.deserializedHotbar != null) {
            holder.putComponent(InventoryComponent.Hotbar.getComponentType(), new InventoryComponent.Hotbar(this.deserializedHotbar, this.deserializedActiveHotbarSlot));
            this.deserializedHotbar = null;
        }
        if (this.deserializedUtility != null) {
            holder.putComponent(InventoryComponent.Utility.getComponentType(), new InventoryComponent.Utility(this.deserializedUtility, this.deserializedActiveUtilitySlot));
            this.deserializedUtility = null;
        }
        if (this.deserializedTools != null) {
            holder.putComponent(InventoryComponent.Tool.getComponentType(), new InventoryComponent.Tool(this.deserializedTools, this.deserializedActiveToolsSlot));
            this.deserializedTools = null;
        }
        if (this.deserializedBackpack != null) {
            holder.putComponent(InventoryComponent.Backpack.getComponentType(), new InventoryComponent.Backpack(this.deserializedBackpack));
            this.deserializedBackpack = null;
        }
    }

    public void backwardsCompatHook(Holder<EntityStore> holder) {
        this.storage = holder.getComponent(InventoryComponent.Storage.getComponentType());
        this.armor = holder.getComponent(InventoryComponent.Armor.getComponentType());
        this.hotbar = holder.getComponent(InventoryComponent.Hotbar.getComponentType());
        this.utility = holder.getComponent(InventoryComponent.Utility.getComponentType());
        this.tools = holder.getComponent(InventoryComponent.Tool.getComponentType());
        this.backpack = holder.getComponent(InventoryComponent.Backpack.getComponentType());
    }

    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Inventory inventory = (Inventory)o;
        if (!Objects.equals(this.storage, inventory.storage)) {
            return false;
        }
        if (!Objects.equals(this.armor, inventory.armor)) {
            return false;
        }
        if (!Objects.equals(this.utility, inventory.utility)) {
            return false;
        }
        if (!Objects.equals(this.tools, inventory.tools)) {
            return false;
        }
        if (!Objects.equals(this.backpack, inventory.backpack)) {
            return false;
        }
        return Objects.equals(this.hotbar, inventory.hotbar);
    }

    public int hashCode() {
        int result = this.storage != null ? this.storage.hashCode() : 0;
        result = 31 * result + (this.armor != null ? this.armor.hashCode() : 0);
        result = 31 * result + (this.hotbar != null ? this.hotbar.hashCode() : 0);
        result = 31 * result + (this.utility != null ? this.utility.hashCode() : 0);
        result = 31 * result + (this.tools != null ? this.tools.hashCode() : 0);
        result = 31 * result + (this.backpack != null ? this.backpack.hashCode() : 0);
        return result;
    }

    @Nonnull
    public String toString() {
        return "Inventory{, storage=" + String.valueOf(this.storage) + ", armor=" + String.valueOf(this.armor) + ", hotbar=" + String.valueOf(this.hotbar) + ", utility=" + String.valueOf(this.utility) + "}";
    }

    public void setUsingToolsItem(boolean value) {
        if (this.tools == null) {
            return;
        }
        this.tools.setUsingToolsItem(value);
    }

    public boolean usingToolsItem() {
        return this.tools != null && this.tools.isUsingToolsItem();
    }

    public static enum ItemPickupType {
        PASSIVE,
        INTERACTION;

    }
}

