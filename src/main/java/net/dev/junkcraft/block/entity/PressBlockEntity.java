package net.dev.junkcraft.block.entity;

import net.dev.junkcraft.block.PressBlock;
import net.dev.junkcraft.energy.ConsumerEnergyStorage;
import net.dev.junkcraft.menu.PressMenu;
import net.dev.junkcraft.recipe.PressRecipe;
import net.dev.junkcraft.recipe.PressRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

public class PressBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENERGY_CAPACITY = 10000;
    public static final int MAX_ENERGY_RECEIVE = 200;

    private final ConsumerEnergyStorage energyStorage = new ConsumerEnergyStorage(ENERGY_CAPACITY, MAX_ENERGY_RECEIVE);

    private final ItemStackHandler inputItems = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            for (PressRecipe recipe : PressRecipes.all()) {
                if (slot == 0 && stack.is(recipe.primary())) {
                    return true;
                }
                if (slot == 1 && recipe.secondary() != null && stack.is(recipe.secondary())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ItemStackHandler outputItems = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final IItemHandler combinedItemHandler = new CombinedInvWrapper(inputItems, outputItems);

    private int progress;
    private int maxProgress;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> progress;
                case 3 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyStorage.setEnergy(value);
                case 2 -> progress = value;
                case 3 -> maxProgress = value;
                default -> {}
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public PressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRESS.get(), pos, state);
    }

    public ConsumerEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public ItemStackHandler getInputItems() {
        return inputItems;
    }

    public ItemStackHandler getOutputItems() {
        return outputItems;
    }

    public IItemHandler getCombinedItemHandler() {
        return combinedItemHandler;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PressBlockEntity be) {
        boolean wasActive = state.getValue(PressBlock.RUNNING);
        boolean active = false;

        ItemStack primaryStack = be.inputItems.getStackInSlot(0);
        ItemStack secondaryStack = be.inputItems.getStackInSlot(1);
        PressRecipe matched = findMatch(primaryStack, secondaryStack);

        if (matched != null && be.canInsertResult(matched.result())) {
            if (be.energyStorage.consumeEnergy(PressRecipe.RF_PER_TICK)) {
                be.progress++;
                be.maxProgress = matched.processTicks();
                active = true;
                if (be.progress >= be.maxProgress) {
                    be.craft(matched);
                    be.progress = 0;
                }
                be.setChanged();
            }
        } else if (be.progress != 0) {
            be.progress = 0;
            be.setChanged();
        }

        if (active != wasActive) {
            level.setBlock(pos, state.setValue(PressBlock.RUNNING, active), 3);
        }
    }

    private static PressRecipe findMatch(ItemStack primaryStack, ItemStack secondaryStack) {
        for (PressRecipe recipe : PressRecipes.all()) {
            if (recipe.matches(primaryStack, secondaryStack)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean canInsertResult(ItemStack result) {
        ItemStack existing = outputItems.getStackInSlot(0);
        if (existing.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(existing, result) && existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    private void craft(PressRecipe recipe) {
        inputItems.extractItem(0, recipe.primaryCount(), false);
        if (recipe.secondary() != null) {
            inputItems.extractItem(1, recipe.secondaryCount(), false);
        }

        ItemStack existing = outputItems.getStackInSlot(0);
        if (existing.isEmpty()) {
            outputItems.setStackInSlot(0, recipe.result().copy());
        } else {
            existing.grow(recipe.result().getCount());
        }
    }

    public NonNullList<ItemStack> getDropStacks() {
        NonNullList<ItemStack> stacks = NonNullList.create();
        stacks.add(inputItems.getStackInSlot(0));
        stacks.add(inputItems.getStackInSlot(1));
        stacks.add(outputItems.getStackInSlot(0));
        return stacks;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("InputItems", inputItems.serializeNBT(registries));
        tag.put("OutputItems", outputItems.serializeNBT(registries));
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inputItems.deserializeNBT(registries, tag.getCompound("InputItems"));
        outputItems.deserializeNBT(registries, tag.getCompound("OutputItems"));
        energyStorage.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.junkcraft.press");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PressMenu(containerId, playerInventory, this.worldPosition, this.inputItems, this.outputItems, this.data);
    }
}
