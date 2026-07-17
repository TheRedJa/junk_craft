package net.dev.junkcraft.block.entity;

import net.dev.junkcraft.block.CoalGeneratorBlock;
import net.dev.junkcraft.energy.GeneratorEnergyStorage;
import net.dev.junkcraft.menu.CoalGeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

public class CoalGeneratorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENERGY_CAPACITY = 32000;
    public static final int MAX_ENERGY_EXTRACT = 200;
    public static final int RF_PER_TICK = 40;
    public static final int FLUID_CAPACITY = 10000;
    public static final int LAVA_MB_PER_TICK = 5;

    private final GeneratorEnergyStorage energyStorage = new GeneratorEnergyStorage(ENERGY_CAPACITY, MAX_ENERGY_EXTRACT);

    private final FluidTank fluidTank = new FluidTank(FLUID_CAPACITY, stack -> stack.is(FluidTags.LAVA));

    private final ItemStackHandler fuelItems = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getBurnTime(null) > 0 && !(stack.getItem() instanceof BucketItem);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int litTime;
    private int litDuration;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> litTime;
                case 3 -> litDuration;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyStorage.setEnergy(value);
                case 2 -> litTime = value;
                case 3 -> litDuration = value;
                default -> {}
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COAL_GENERATOR.get(), pos, state);
    }

    public GeneratorEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public ItemStackHandler getFuelItems() {
        return fuelItems;
    }

    public NonNullList<ItemStack> getDropStacks() {
        NonNullList<ItemStack> stacks = NonNullList.create();
        stacks.add(fuelItems.getStackInSlot(0));
        return stacks;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoalGeneratorBlockEntity be) {
        boolean wasLit = state.getValue(CoalGeneratorBlock.LIT);
        boolean isActive = false;
        boolean dirty = false;

        if (be.litTime > 0) {
            be.litTime--;
            be.energyStorage.addEnergy(RF_PER_TICK);
            isActive = true;
            dirty = true;
        } else {
            ItemStack fuel = be.fuelItems.getStackInSlot(0);
            int burnTime = fuel.getBurnTime(null);
            int room = be.energyStorage.getMaxEnergyStored() - be.energyStorage.getEnergyStored();
            if (!fuel.isEmpty() && burnTime > 0 && room >= RF_PER_TICK) {
                fuel.shrink(1);
                be.litDuration = burnTime;
                be.litTime = burnTime - 1;
                be.energyStorage.addEnergy(RF_PER_TICK);
                isActive = true;
                dirty = true;
            }
        }

        int room = be.energyStorage.getMaxEnergyStored() - be.energyStorage.getEnergyStored();
        if (!be.fluidTank.isEmpty() && room >= RF_PER_TICK) {
            be.fluidTank.drain(LAVA_MB_PER_TICK, IFluidHandler.FluidAction.EXECUTE);
            be.energyStorage.addEnergy(RF_PER_TICK);
            isActive = true;
            dirty = true;
        }

        be.distributeEnergy(level, pos);

        if (isActive != wasLit) {
            level.setBlock(pos, state.setValue(CoalGeneratorBlock.LIT, isActive), 3);
        }
        if (dirty) {
            be.setChanged();
        }
    }

    private void distributeEnergy(Level level, BlockPos pos) {
        if (energyStorage.getEnergyStored() <= 0) {
            return;
        }
        for (Direction direction : Direction.values()) {
            if (energyStorage.getEnergyStored() <= 0) {
                break;
            }
            IEnergyStorage neighbor = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(direction), direction.getOpposite());
            if (neighbor != null && neighbor.canReceive()) {
                int simulated = energyStorage.extractEnergy(MAX_ENERGY_EXTRACT, true);
                if (simulated > 0) {
                    int accepted = neighbor.receiveEnergy(simulated, false);
                    if (accepted > 0) {
                        energyStorage.extractEnergy(accepted, false);
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("FuelItems", fuelItems.serializeNBT(registries));
        tag.put("Tank", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putInt("LitTime", litTime);
        tag.putInt("LitDuration", litDuration);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuelItems.deserializeNBT(registries, tag.getCompound("FuelItems"));
        fluidTank.readFromNBT(registries, tag.getCompound("Tank"));
        energyStorage.setEnergy(tag.getInt("Energy"));
        litTime = tag.getInt("LitTime");
        litDuration = tag.getInt("LitDuration");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.junkcraft.coal_generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CoalGeneratorMenu(containerId, playerInventory, this.worldPosition, this.fuelItems, this.data);
    }
}
