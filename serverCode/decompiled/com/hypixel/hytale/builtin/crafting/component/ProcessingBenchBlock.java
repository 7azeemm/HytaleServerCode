/*
 * Decompiled with CFR 0.152.
 */
package com.hypixel.hytale.builtin.crafting.component;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.builtin.crafting.window.BenchWindow;
import com.hypixel.hytale.builtin.crafting.window.ProcessingBenchWindow;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.BenchTierLevel;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.ProcessingBench;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.ResourceQuantity;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.InternalContainerUtilMaterial;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.ResourceFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ResourceTransaction;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class ProcessingBenchBlock
implements Component<ChunkStore> {
    @Nonnull
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final boolean EXACT_RESOURCE_AMOUNTS = true;
    @Nonnull
    public static final BuilderCodec<ProcessingBenchBlock> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ProcessingBenchBlock.class, ProcessingBenchBlock::new).append(new KeyedCodec<ItemContainer>("InputContainer", ItemContainer.CODEC), (state, o) -> {
        state.inputContainer = o;
    }, state -> state.inputContainer).add()).append(new KeyedCodec<ItemContainer>("FuelContainer", ItemContainer.CODEC), (state, o) -> {
        state.fuelContainer = o;
    }, state -> state.fuelContainer).add()).append(new KeyedCodec<ItemContainer>("OutputContainer", ItemContainer.CODEC), (state, o) -> {
        state.outputContainer = o;
    }, state -> state.outputContainer).add()).append(new KeyedCodec<Double>("Progress", Codec.DOUBLE), (state, d) -> {
        state.inputProgress = d.floatValue();
    }, state -> state.inputProgress).add()).append(new KeyedCodec<Double>("FuelTime", Codec.DOUBLE), (state, d) -> {
        state.fuelTime = d.floatValue();
    }, state -> state.fuelTime).add()).append(new KeyedCodec<Boolean>("Active", Codec.BOOLEAN), (state, b) -> {
        state.active = b;
    }, state -> state.active).add()).append(new KeyedCodec<Integer>("NextExtra", Codec.INTEGER), (state, b) -> {
        state.nextExtra = b;
    }, state -> state.nextExtra).add()).append(new KeyedCodec<String>("RecipeId", Codec.STRING), (state, o) -> {
        state.recipeId = o;
    }, state -> state.recipeId).add()).append(new KeyedCodec("LastTickGameTime", Codec.INSTANT), (state, t) -> {
        state.lastTickGameTime = t;
    }, state -> state.lastTickGameTime).add()).build();
    private static final float MAX_UNLOAD_ELAPSED_SECONDS = 86400.0f;
    private static final float EJECT_VELOCITY = 2.0f;
    private static final float EJECT_SPREAD_VELOCITY = 1.0f;
    private static final float EJECT_VERTICAL_VELOCITY = 3.25f;
    @Nonnull
    public static final String PROCESSING = "Processing";
    @Nonnull
    public static final String PROCESS_COMPLETED = "ProcessCompleted";
    private transient Bench bench;
    private transient ProcessingBench processingBench;
    private ItemContainer inputContainer;
    private ItemContainer fuelContainer;
    private ItemContainer outputContainer;
    private CombinedItemContainer combinedItemContainer;
    private float inputProgress;
    private float fuelTime;
    private int lastConsumedFuelTotal;
    private int nextExtra = -1;
    @Nonnull
    private final Set<Short> processingSlots = new HashSet<Short>();
    @Nonnull
    private final Set<Short> processingFuelSlots = new HashSet<Short>();
    @Nullable
    private String recipeId;
    @Nullable
    private CraftingRecipe recipe;
    private boolean active = false;
    @Nullable
    private Instant lastTickGameTime;

    public static ComponentType<ChunkStore, ProcessingBenchBlock> getComponentType() {
        return CraftingPlugin.get().getProcessingBenchBlockComponentType();
    }

    @Nullable
    public Bench getBench() {
        return this.bench;
    }

    @Nullable
    public ProcessingBench getProcessingBench() {
        return this.processingBench;
    }

    public ItemContainer getOutputContainer() {
        return this.outputContainer;
    }

    public ItemContainer getInputContainer() {
        return this.inputContainer;
    }

    public ItemContainer getFuelContainer() {
        return this.fuelContainer;
    }

    public float getFuelTime() {
        return this.fuelTime;
    }

    @Nonnull
    public Set<Short> getProcessingSlots() {
        return this.processingSlots;
    }

    @Nonnull
    public Set<Short> getProcessingFuelSlots() {
        return this.processingFuelSlots;
    }

    public void setInputProgress(float inputProgress) {
        this.inputProgress = inputProgress;
    }

    @Nullable
    public Instant getLastTickGameTime() {
        return this.lastTickGameTime;
    }

    public void setLastTickGameTime(@Nullable Instant lastTickGameTime) {
        this.lastTickGameTime = lastTickGameTime;
    }

    public void setLastConsumedFuelTotal(int lastConsumedFuelTotal) {
        this.lastConsumedFuelTotal = lastConsumedFuelTotal;
    }

    public void clearCurrentRecipe() {
        this.recipeId = null;
        this.recipe = null;
    }

    public boolean initializeBenchConfig(@Nonnull BlockType blockType) {
        Bench blockBench = blockType.getBench();
        if (blockBench == null) {
            return false;
        }
        if (!(blockBench instanceof ProcessingBench)) {
            LOGGER.at(Level.SEVERE).log("Wrong bench type for processing. Got %s", blockBench.getClass().getName());
            return false;
        }
        this.bench = blockBench;
        this.processingBench = (ProcessingBench)blockBench;
        if (this.nextExtra == -1) {
            this.nextExtra = this.processingBench.getExtraOutput() != null ? this.processingBench.getExtraOutput().getPerFuelItemsConsumed() : 0;
        }
        return true;
    }

    public void setupSlots(@Nonnull World world, @Nonnull BenchBlock benchBlock, @Nonnull BlockModule.BlockStateInfo blockStateInfo, int blockX, int blockY, int blockZ, @Nonnull BlockType blockType, int rotationIndex) {
        String resourceTypeId;
        ObjectArrayList<ItemStack> remainder = new ObjectArrayList<ItemStack>();
        int tierLevel = benchBlock.getTierLevel();
        ProcessingBench.ProcessingSlot[] input = this.processingBench.getInput(tierLevel);
        short inputSlotsCount = (short)input.length;
        this.inputContainer = ItemContainer.ensureContainerCapacity(this.inputContainer, inputSlotsCount, SimpleItemContainer::getNewContainer, remainder);
        this.inputContainer.registerChangeEvent(EventPriority.LAST, event -> blockStateInfo.markNeedsSaving());
        for (short slot = 0; slot < inputSlotsCount; slot = (short)(slot + 1)) {
            ProcessingBench.ProcessingSlot inputSlot = input[slot];
            resourceTypeId = inputSlot.getResourceTypeId();
            boolean shouldFilterValidIngredients = inputSlot.shouldFilterValidIngredients();
            if (resourceTypeId != null) {
                this.inputContainer.setSlotFilter(FilterActionType.ADD, slot, new ResourceFilter(new ResourceQuantity(resourceTypeId, 1)));
                continue;
            }
            if (!shouldFilterValidIngredients) continue;
            ObjectArrayList<MaterialQuantity> validIngredients = new ObjectArrayList<MaterialQuantity>();
            List<CraftingRecipe> recipes = CraftingPlugin.getBenchRecipes(this.bench.getType(), this.bench.getId());
            for (CraftingRecipe recipe : recipes) {
                if (recipe.isRestrictedByBenchTierLevel(this.bench.getId(), tierLevel)) continue;
                List<MaterialQuantity> inputMaterials = CraftingManager.getInputMaterials(recipe);
                validIngredients.addAll((Collection<MaterialQuantity>)inputMaterials);
            }
            this.inputContainer.setSlotFilter(FilterActionType.ADD, slot, (actionType, container, slotIndex, itemStack) -> {
                if (itemStack == null) {
                    return true;
                }
                for (MaterialQuantity ingredient : validIngredients) {
                    if (!CraftingManager.matches(ingredient, itemStack)) continue;
                    return true;
                }
                return false;
            });
        }
        ProcessingBench.ProcessingSlot[] benchFuel = this.processingBench.getFuel();
        short fuelCapacity = (short)(benchFuel != null ? benchFuel.length : 0);
        this.fuelContainer = ItemContainer.ensureContainerCapacity(this.fuelContainer, fuelCapacity, SimpleItemContainer::getNewContainer, remainder);
        this.fuelContainer.registerChangeEvent(EventPriority.LAST, event -> blockStateInfo.markNeedsSaving());
        if (fuelCapacity > 0) {
            for (int i = 0; i < benchFuel.length; ++i) {
                ProcessingBench.ProcessingSlot fuel = benchFuel[i];
                resourceTypeId = fuel.getResourceTypeId();
                if (resourceTypeId == null) continue;
                this.fuelContainer.setSlotFilter(FilterActionType.ADD, (short)i, new ResourceFilter(new ResourceQuantity(resourceTypeId, 1)));
            }
        }
        short outputSlotsCount = (short)this.processingBench.getOutputSlotsCount(tierLevel);
        this.outputContainer = ItemContainer.ensureContainerCapacity(this.outputContainer, outputSlotsCount, SimpleItemContainer::getNewContainer, remainder);
        this.outputContainer.registerChangeEvent(EventPriority.LAST, event -> blockStateInfo.markNeedsSaving());
        if (outputSlotsCount > 0) {
            this.outputContainer.setGlobalFilter(FilterType.ALLOW_OUTPUT_ONLY);
        }
        this.combinedItemContainer = new CombinedItemContainer(this.fuelContainer, this.inputContainer, this.outputContainer);
        Store<EntityStore> entityStore = world.getEntityStore().getStore();
        Holder[] itemEntityHolders = this.ejectItems(entityStore, remainder, rotationIndex, blockType, blockX, blockY, blockZ);
        if (itemEntityHolders.length > 0) {
            world.execute(() -> entityStore.addEntities(itemEntityHolders, AddReason.SPAWN));
        }
        this.inputContainer.registerChangeEvent(EventPriority.LAST, event -> this.updateRecipe(benchBlock));
        this.outputContainer.registerChangeEvent(EventPriority.LAST, event -> this.updateRecipe(benchBlock));
        if (this.processingBench.getFuel() == null) {
            this.setActive(true, benchBlock, blockStateInfo);
        }
        if (this.lastTickGameTime != null) {
            Instant currentGameTime = entityStore.getResource(WorldTimeResource.getResourceType()).getGameTime();
            float gameElapsedSeconds = (float)Math.max(0L, currentGameTime.toEpochMilli() - this.lastTickGameTime.toEpochMilli()) / 1000.0f;
            float realElapsedSeconds = (float)((double)gameElapsedSeconds / WorldTimeResource.getSecondsPerTick(world));
            realElapsedSeconds = Math.min(realElapsedSeconds, 86400.0f);
            this.checkForRecipeUpdate(benchBlock);
            this.advanceProcessing(realElapsedSeconds, entityStore, benchBlock, blockStateInfo, blockX, blockY, blockZ, blockType, rotationIndex);
            this.lastTickGameTime = currentGameTime;
            blockStateInfo.markNeedsSaving();
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean setActive(boolean active, @Nonnull BenchBlock benchBlock, @Nullable BlockModule.BlockStateInfo blockStateInfo) {
        if (this.active != active) {
            if (active && this.processingBench.getFuel() != null && this.fuelContainer.isEmpty()) {
                return false;
            }
            this.active = active;
            Map<UUID, BenchWindow> windows = benchBlock.getWindows();
            if (!active) {
                this.processingSlots.clear();
                this.processingFuelSlots.clear();
                this.sendProcessingSlots(windows);
                this.sendProcessingFuelSlots(windows);
            }
            this.updateRecipe(benchBlock);
            windows.forEach((uuid, window) -> ((ProcessingBenchWindow)window).setActive(active));
            if (blockStateInfo != null) {
                blockStateInfo.markNeedsSaving();
            }
            return true;
        }
        return false;
    }

    public void updateFuelValues(@Nonnull Map<UUID, BenchWindow> windows) {
        if (this.fuelTime > (float)this.lastConsumedFuelTotal) {
            this.lastConsumedFuelTotal = MathUtil.ceil(this.fuelTime);
        }
        float fuelPercent = this.lastConsumedFuelTotal > 0 ? this.fuelTime / (float)this.lastConsumedFuelTotal : 0.0f;
        windows.forEach((uuid, window) -> {
            ProcessingBenchWindow processingBenchWindow = (ProcessingBenchWindow)window;
            processingBenchWindow.setFuelTime(fuelPercent);
            processingBenchWindow.setMaxFuel(this.lastConsumedFuelTotal);
            processingBenchWindow.setProcessingFuelSlots(this.processingFuelSlots);
        });
    }

    @Nullable
    public CombinedItemContainer getItemContainer() {
        return this.combinedItemContainer;
    }

    @Nullable
    public CraftingRecipe getRecipe() {
        return this.recipe;
    }

    public float getInputProgress() {
        return this.inputProgress;
    }

    public void dropFuelItems(@Nonnull List<ItemStack> itemStacks) {
        String fuelDropItemId = this.processingBench.getFuelDropItemId();
        if (fuelDropItemId != null) {
            int quantity;
            Item item = Item.getAssetMap().getAsset(fuelDropItemId);
            this.fuelTime = 0.0f;
            for (int dropAmount = (int)this.fuelTime; dropAmount > 0; dropAmount -= quantity) {
                quantity = Math.min(dropAmount, item.getMaxStack());
                itemStacks.add(new ItemStack(fuelDropItemId, quantity));
            }
        } else {
            LOGGER.at(Level.WARNING).log("No FuelDropItemId defined for %s fuel value of %s will be lost!", (Object)this.bench.getId(), this.fuelTime);
        }
    }

    private float getCraftingTimeReductionModifier(int tierLevel) {
        BenchTierLevel levelData = this.bench.getTierLevel(tierLevel);
        return levelData != null ? levelData.getCraftingTimeReductionModifier() : 0.0f;
    }

    public float getRecipeTimeSeconds(int tierLevel) {
        if (this.recipe == null) {
            return 0.0f;
        }
        float t = this.recipe.getTimeSeconds();
        float mod = this.getCraftingTimeReductionModifier(tierLevel);
        return mod > 0.0f ? t - t * mod : t;
    }

    private int countAvailableInputSets() {
        if (this.recipe == null) {
            return 0;
        }
        List<MaterialQuantity> inputMaterials = CraftingManager.getInputMaterials(this.recipe);
        if (inputMaterials.isEmpty()) {
            return 0;
        }
        int minSets = Integer.MAX_VALUE;
        for (MaterialQuantity material : inputMaterials) {
            int required = material.getQuantity();
            int available = 0;
            for (short s = 0; s < this.inputContainer.getCapacity(); s = (short)(s + 1)) {
                ItemStack stack = this.inputContainer.getItemStack(s);
                if (stack == null || !CraftingManager.matches(material, stack)) continue;
                available += stack.getQuantity();
            }
            minSets = Math.min(minSets, available / required);
        }
        return minSets;
    }

    private float calculateTotalAvailableFuel() {
        ProcessingBench.ProcessingSlot[] fuelSlots = this.processingBench.getFuel();
        if (fuelSlots == null) {
            return 0.0f;
        }
        float total = 0.0f;
        for (int i = 0; i < fuelSlots.length; ++i) {
            ItemStack stack = this.fuelContainer.getItemStack((short)i);
            if (stack == null) continue;
            total += (float)((double)stack.getQuantity() * stack.getItem().getFuelQuality());
        }
        return total;
    }

    private boolean canFitScaledOutput(@Nonnull List<ItemStack> outputPerRecipe, int count) {
        if (count <= 0) {
            return true;
        }
        ObjectArrayList<ItemStack> scaled = new ObjectArrayList<ItemStack>();
        for (ItemStack item : outputPerRecipe) {
            int qty;
            int maxStack = item.getItem().getMaxStack();
            for (int total = item.getQuantity() * count; total > 0; total -= qty) {
                qty = Math.min(total, maxStack);
                scaled.add(item.withQuantity(qty));
            }
        }
        return this.outputContainer.canAddItemStacks(scaled, false, false);
    }

    private void completeRecipes(int count, @Nonnull Store<EntityStore> entityStore, int blockX, int blockY, int blockZ, @Nonnull BlockType blockType, int rotationIndex) {
        if (this.recipe == null || count <= 0) {
            return;
        }
        List<MaterialQuantity> inputMaterials = CraftingManager.getInputMaterials(this.recipe);
        List<ItemStack> outputItemStacks = CraftingManager.getOutputItemStacks(this.recipe);
        ObjectArrayList<MaterialQuantity> scaledInput = new ObjectArrayList<MaterialQuantity>(inputMaterials.size());
        for (MaterialQuantity m : inputMaterials) {
            scaledInput.add(m.clone(m.getQuantity() * count));
        }
        ListTransaction<MaterialTransaction> removeTransaction = this.inputContainer.removeMaterials(scaledInput, true, true, true);
        if (!removeTransaction.succeeded()) {
            return;
        }
        ObjectArrayList<ItemStack> scaledOutput = new ObjectArrayList<ItemStack>();
        for (ItemStack item : outputItemStacks) {
            int qty;
            int maxStack = item.getItem().getMaxStack();
            for (int total = item.getQuantity() * count; total > 0; total -= qty) {
                qty = Math.min(total, maxStack);
                scaledOutput.add(item.withQuantity(qty));
            }
        }
        this.addOutputAndEjectRemainder(scaledOutput, entityStore, blockX, blockY, blockZ, blockType, rotationIndex);
    }

    public int advanceProcessing(float dt, @Nonnull Store<EntityStore> entityStore, @Nonnull BenchBlock benchBlock, @Nonnull BlockModule.BlockStateInfo blockStateInfo, int blockX, int blockY, int blockZ, @Nonnull BlockType blockType, int rotationIndex) {
        int completions;
        boolean hasFuelSlots;
        if (this.recipe == null || dt <= 0.0f) {
            return 0;
        }
        boolean bl = hasFuelSlots = this.processingBench.getFuel() != null;
        if (hasFuelSlots && !this.active) {
            return 0;
        }
        int tierLevel = benchBlock.getTierLevel();
        float recipeTime = this.getRecipeTimeSeconds(tierLevel);
        if (recipeTime <= 0.0f) {
            int completed = 0;
            while (this.tryCompleteOneRecipe(entityStore, blockX, blockY, blockZ, blockType, rotationIndex)) {
                ++completed;
                this.updateRecipe(benchBlock);
                if (this.recipe != null) continue;
            }
            if (hasFuelSlots && this.active && this.recipe == null) {
                this.setActive(false, benchBlock, blockStateInfo);
            }
            return completed;
        }
        float startProgress = this.inputProgress;
        int maxFromTime = (int)((this.inputProgress + dt) / recipeTime);
        int maxFromInput = this.countAvailableInputSets();
        int maxFromFuel = Integer.MAX_VALUE;
        if (hasFuelSlots) {
            float totalFuel = this.fuelTime + this.calculateTotalAvailableFuel();
            maxFromFuel = (int)((this.inputProgress + totalFuel) / recipeTime);
        }
        if ((completions = Math.min(maxFromTime, Math.min(maxFromInput, maxFromFuel))) > 0) {
            List<ItemStack> outputStacks = CraftingManager.getOutputItemStacks(this.recipe);
            int lo = 0;
            int hi = completions;
            while (lo < hi) {
                int mid = (lo + hi + 1) / 2;
                if (this.canFitScaledOutput(outputStacks, mid)) {
                    lo = mid;
                    continue;
                }
                hi = mid - 1;
            }
            completions = lo;
        }
        float timeForCompletions = completions > 0 ? (float)completions * recipeTime - this.inputProgress : 0.0f;
        timeForCompletions = Math.max(0.0f, timeForCompletions);
        boolean hasMoreInput = completions < maxFromInput;
        float partialTime = hasMoreInput ? Math.min(dt - timeForCompletions, recipeTime) : 0.0f;
        partialTime = Math.max(0.0f, partialTime);
        if (hasFuelSlots && partialTime > 0.0f) {
            float totalFuel = this.fuelTime + this.calculateTotalAvailableFuel();
            partialTime = Math.min(partialTime, Math.max(0.0f, totalFuel - timeForCompletions));
        }
        float totalTimeUsed = timeForCompletions + partialTime;
        if (hasFuelSlots && totalTimeUsed > 0.0f) {
            this.consumeFuelForDuration(totalTimeUsed, entityStore, blockX, blockY, blockZ, blockType, rotationIndex);
        }
        if (completions > 0) {
            this.completeRecipes(completions, entityStore, blockX, blockY, blockZ, blockType, rotationIndex);
        }
        this.inputProgress = startProgress + totalTimeUsed - (float)completions * recipeTime;
        if (this.inputProgress < 0.0f) {
            this.inputProgress = 0.0f;
        }
        this.updateRecipe(benchBlock);
        if (hasFuelSlots && this.active && this.recipe == null) {
            this.setActive(false, benchBlock, blockStateInfo);
        }
        return completions;
    }

    private int consumeOneFuel(@Nonnull Store<EntityStore> entityStore, int blockX, int blockY, int blockZ, @Nonnull BlockType blockType, int rotationIndex) {
        ProcessingBench.ProcessingSlot[] fuelSlots = this.processingBench.getFuel();
        if (fuelSlots == null || fuelSlots.length == 0 || !this.active || this.processingBench.getMaxFuel() > 0 && !(this.fuelTime < (float)this.processingBench.getMaxFuel()) || this.fuelContainer.isEmpty()) {
            return -1;
        }
        if (this.fuelTime < 0.0f) {
            this.fuelTime = 0.0f;
        }
        for (int i = 0; i < fuelSlots.length; ++i) {
            ProcessingBench.ProcessingSlot fuelSlot = fuelSlots[i];
            String resourceTypeId = fuelSlot.getResourceTypeId() != null ? fuelSlot.getResourceTypeId() : "Fuel";
            ResourceQuantity resourceQuantity = new ResourceQuantity(resourceTypeId, 1);
            ItemStack slot = this.fuelContainer.getItemStack((short)i);
            if (slot == null) continue;
            double fuelQuality = slot.getItem().getFuelQuality();
            ResourceTransaction transaction = this.fuelContainer.removeResource(resourceQuantity, true, true, true);
            if (transaction.getRemainder() > 0) continue;
            ProcessingBench.ExtraOutput extra = this.processingBench.getExtraOutput();
            if (extra != null && !extra.isIgnoredFuelSource(slot.getItem())) {
                --this.nextExtra;
                if (this.nextExtra <= 0) {
                    this.nextExtra = extra.getPerFuelItemsConsumed();
                    ObjectArrayList<ItemStack> extraItemStacks = new ObjectArrayList<ItemStack>(extra.getOutputs().length);
                    for (MaterialQuantity e : extra.getOutputs()) {
                        extraItemStacks.add(e.toItemStack());
                    }
                    this.addOutputAndEjectRemainder(extraItemStacks, entityStore, blockX, blockY, blockZ, blockType, rotationIndex);
                }
            }
            this.fuelTime += (float)((double)transaction.getConsumed() * fuelQuality);
            return i;
        }
        return -1;
    }

    private void addOutputAndEjectRemainder(@Nonnull List<ItemStack> outputItemStacks, @Nonnull Store<EntityStore> entityStore, int blockX, int blockY, int blockZ, @Nonnull BlockType blockType, int rotationIndex) {
        ListTransaction<ItemStackTransaction> addTransaction = this.outputContainer.addItemStacks(outputItemStacks, false, false, false);
        ObjectArrayList<ItemStack> remainderItems = new ObjectArrayList<ItemStack>();
        for (ItemStackTransaction itemStackTransaction : addTransaction.getList()) {
            ItemStack remainder = itemStackTransaction.getRemainder();
            if (remainder == null || remainder.isEmpty()) continue;
            remainderItems.add(remainder);
        }
        if (!remainderItems.isEmpty()) {
            Holder<EntityStore>[] holders = this.ejectItems(entityStore, remainderItems, rotationIndex, blockType, blockX, blockY, blockZ);
            entityStore.addEntities(holders, AddReason.SPAWN);
        }
    }

    private boolean tryCompleteOneRecipe(@Nonnull Store<EntityStore> entityStore, int blockX, int blockY, int blockZ, @Nonnull BlockType blockType, int rotationIndex) {
        if (this.recipe == null) {
            return false;
        }
        float recipeTime = this.recipe.getTimeSeconds();
        if (this.inputProgress < recipeTime) {
            return false;
        }
        List<MaterialQuantity> inputMaterials = CraftingManager.getInputMaterials(this.recipe);
        List<ItemStack> outputItemStacks = CraftingManager.getOutputItemStacks(this.recipe);
        if (!this.outputContainer.canAddItemStacks(outputItemStacks, false, false)) {
            return false;
        }
        if (this.inputContainer.getSlotMaterialsToRemove(inputMaterials, true, true).isEmpty()) {
            return false;
        }
        ListTransaction<MaterialTransaction> transaction = this.inputContainer.removeMaterials(inputMaterials, true, true, true);
        if (!transaction.succeeded()) {
            return false;
        }
        this.inputProgress -= recipeTime;
        this.addOutputAndEjectRemainder(outputItemStacks, entityStore, blockX, blockY, blockZ, blockType, rotationIndex);
        return true;
    }

    public void consumeFuelForDuration(float duration, @Nonnull Store<EntityStore> entityStore, int blockX, int blockY, int blockZ, @Nonnull BlockType blockType, int rotationIndex) {
        float use;
        if (duration <= 0.0f) {
            return;
        }
        float consumed = 0.0f;
        if (this.fuelTime > 0.0f) {
            use = Math.min(this.fuelTime, duration);
            this.fuelTime -= use;
            consumed += use;
        }
        while (consumed < duration && this.consumeOneFuel(entityStore, blockX, blockY, blockZ, blockType, rotationIndex) >= 0) {
            use = Math.min(this.fuelTime, duration - consumed);
            this.fuelTime -= use;
            consumed += use;
        }
    }

    @Nonnull
    private Holder<EntityStore>[] ejectItems(@Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull List<ItemStack> itemStacks, int rotationIndex, @Nullable BlockType blockType, int blockX, int blockY, int blockZ) {
        Vector3d dropPosition;
        if (itemStacks.isEmpty()) {
            return Holder.emptyArray();
        }
        RotationTuple rotation = RotationTuple.get(rotationIndex);
        Vector3d frontDir = new Vector3d(0.0, 0.0, 1.0);
        rotation.yaw().rotateY(frontDir, frontDir);
        if (blockType == null) {
            dropPosition = new Vector3d((double)blockX + 0.5, blockY, (double)blockZ + 0.5);
        } else {
            BlockBoundingBoxes hitboxAsset = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
            if (hitboxAsset == null) {
                dropPosition = new Vector3d((double)blockX + 0.5, blockY, (double)blockZ + 0.5);
            } else {
                double depth = hitboxAsset.get(0).getBoundingBox().depth();
                double frontOffset = depth / 2.0 + (double)0.1f;
                dropPosition = ProcessingBenchBlock.getCenteredBlockPosition(blockType, rotationIndex, blockX, blockY, blockZ);
                dropPosition.add(frontDir.x * frontOffset, 0.0, frontDir.z * frontOffset);
            }
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        ObjectArrayList<Holder<EntityStore>> result = new ObjectArrayList<Holder<EntityStore>>(itemStacks.size());
        for (ItemStack item : itemStacks) {
            float velocityZ;
            float velocityX;
            Holder<EntityStore> holder = ItemComponent.generateItemDrop(accessor, item, dropPosition, Vector3f.ZERO, velocityX = (float)(frontDir.x * 2.0 + 2.0 * (random.nextDouble() - 0.5)), 3.25f, velocityZ = (float)(frontDir.z * 2.0 + 2.0 * (random.nextDouble() - 0.5)));
            if (holder == null) continue;
            result.add(holder);
        }
        return (Holder[])result.toArray(Holder[]::new);
    }

    @Nonnull
    private static Vector3d getCenteredBlockPosition(@Nonnull BlockType blockType, int rotationIndex, int blockX, int blockY, int blockZ) {
        Vector3d blockCenter = new Vector3d();
        blockType.getBlockCenter(rotationIndex, blockCenter);
        return blockCenter.add(blockX, blockY, blockZ);
    }

    public void sendProgress(float progress, @Nonnull Map<UUID, BenchWindow> windows) {
        windows.forEach((uuid, window) -> ((ProcessingBenchWindow)window).setProgress(progress));
    }

    public void sendProcessingSlots(@Nonnull Map<UUID, BenchWindow> windows) {
        windows.forEach((uuid, window) -> ((ProcessingBenchWindow)window).setProcessingSlots(this.processingSlots));
    }

    public void sendProcessingFuelSlots(@Nonnull Map<UUID, BenchWindow> windows) {
        windows.forEach((uuid, window) -> ((ProcessingBenchWindow)window).setProcessingFuelSlots(this.processingFuelSlots));
    }

    public void setBlockInteractionState(@Nonnull String state, @Nonnull BlockType blockType, @Nonnull World world, int blockX, int blockY, int blockZ) {
        world.setBlockInteractionState(new Vector3i(blockX, blockY, blockZ), blockType, state);
    }

    public void playSound(int soundEventIndex, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull BlockType blockType, int rotationIndex, int blockX, int blockY, int blockZ) {
        if (soundEventIndex == 0) {
            return;
        }
        Vector3d soundPos = ProcessingBenchBlock.getCenteredBlockPosition(blockType, rotationIndex, blockX, blockY, blockZ);
        SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, soundPos, componentAccessor);
    }

    public void checkForRecipeUpdate(@Nonnull BenchBlock benchBlock) {
        if (this.recipe == null && this.recipeId != null) {
            this.updateRecipe(benchBlock);
        }
    }

    private void updateRecipe(@Nonnull BenchBlock benchBlock) {
        Map<UUID, BenchWindow> windows = benchBlock.getWindows();
        int tierLevel = benchBlock.getTierLevel();
        List<CraftingRecipe> recipes = CraftingPlugin.getBenchRecipes(this.bench.getType(), this.bench.getId());
        if (recipes.isEmpty()) {
            this.clearRecipe(windows);
            return;
        }
        ObjectArrayList matching = new ObjectArrayList();
        for (CraftingRecipe recipe : recipes) {
            if (recipe.isRestrictedByBenchTierLevel(this.bench.getId(), tierLevel)) continue;
            MaterialQuantity[] input = recipe.getInput();
            int matches = 0;
            IntArrayList slots = new IntArrayList();
            for (int j = 0; j < this.inputContainer.getCapacity(); ++j) {
                slots.add(j);
            }
            block2: for (MaterialQuantity craftingMaterial : input) {
                String itemId = craftingMaterial.getItemId();
                String resourceTypeId = craftingMaterial.getResourceTypeId();
                int materialQuantity = craftingMaterial.getQuantity();
                BsonDocument metadata = craftingMaterial.getMetadata();
                MaterialQuantity material = new MaterialQuantity(itemId, resourceTypeId, null, materialQuantity, metadata);
                for (int k = 0; k < slots.size(); ++k) {
                    int j = slots.getInt(k);
                    int out = InternalContainerUtilMaterial.testRemoveMaterialFromSlot(this.inputContainer, (short)j, material, material.getQuantity(), true);
                    if (out != 0) continue;
                    ++matches;
                    slots.removeInt(k);
                    continue block2;
                }
            }
            if (matches != input.length) continue;
            matching.add(recipe);
        }
        if (matching.isEmpty()) {
            this.clearRecipe(windows);
            return;
        }
        matching.sort(Comparator.comparingInt(o -> CraftingManager.getInputMaterials(o).size()));
        Collections.reverse(matching);
        if (this.recipeId != null) {
            for (CraftingRecipe rec : matching) {
                if (!Objects.equals(this.recipeId, rec.getId())) continue;
                LOGGER.at(Level.FINE).log("Keeping existing Recipe %s %s", (Object)this.recipeId, (Object)rec);
                this.recipe = rec;
                return;
            }
        }
        CraftingRecipe recipe = (CraftingRecipe)matching.getFirst();
        if (this.recipeId == null || !Objects.equals(this.recipeId, recipe.getId())) {
            this.inputProgress = 0.0f;
            this.sendProgress(0.0f, windows);
        }
        this.recipeId = recipe.getId();
        this.recipe = recipe;
        LOGGER.at(Level.FINE).log("Found Recipe %s %s", (Object)this.recipeId, (Object)this.recipe);
    }

    private void clearRecipe(@Nonnull Map<UUID, BenchWindow> windows) {
        this.recipeId = null;
        this.recipe = null;
        this.lastConsumedFuelTotal = 0;
        this.inputProgress = 0.0f;
        this.sendProgress(0.0f, windows);
        LOGGER.at(Level.FINE).log("Cleared Recipe");
    }

    @Override
    @Nullable
    public Component<ChunkStore> clone() {
        ProcessingBenchBlock clone = new ProcessingBenchBlock();
        clone.inputContainer = this.inputContainer;
        clone.fuelContainer = this.fuelContainer;
        clone.outputContainer = this.outputContainer;
        clone.inputProgress = this.inputProgress;
        clone.fuelTime = this.fuelTime;
        clone.lastConsumedFuelTotal = this.lastConsumedFuelTotal;
        clone.active = this.active;
        clone.nextExtra = this.nextExtra;
        clone.recipeId = this.recipeId;
        clone.lastTickGameTime = this.lastTickGameTime;
        return clone;
    }
}

