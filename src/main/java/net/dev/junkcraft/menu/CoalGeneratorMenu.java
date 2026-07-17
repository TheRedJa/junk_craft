package net.dev.junkcraft.menu;

import net.dev.junkcraft.block.entity.CoalGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class CoalGeneratorMenu extends AbstractContainerMenu {
    private static final int FUEL_SLOT_X = 80;
    private static final int FUEL_SLOT_Y = 61;
    private static final int PLAYER_INV_Y = 84;
    private static final int PLAYER_HOTBAR_Y = 142;

    public final BlockPos blockPos;
    private final IItemHandler fuelItems;
    public final ContainerData data;

    // Client-side constructor, invoked via ModMenuTypes' IContainerFactory
    public CoalGeneratorMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, pos, new ItemStackHandler(1), new SimpleContainerData(5));
    }

    // Server-side constructor, invoked from CoalGeneratorBlockEntity#createMenu
    public CoalGeneratorMenu(int containerId, Inventory playerInventory, BlockPos pos, IItemHandler fuelItems, ContainerData data) {
        super(ModMenuTypes.COAL_GENERATOR.get(), containerId);
        this.blockPos = pos;
        this.fuelItems = fuelItems;
        this.data = data;

        this.addSlot(new SlotItemHandler(fuelItems, 0, FUEL_SLOT_X, FUEL_SLOT_Y));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, PLAYER_HOTBAR_Y));
        }

        this.addDataSlots(data);
    }

    public int getEnergyStored() {
        return data.get(0);
    }

    public int getMaxEnergyStored() {
        return data.get(1);
    }

    public int getLitTime() {
        return data.get(2);
    }

    public int getLitDuration() {
        return data.get(3);
    }

    public int getGenerationRate() {
        return data.get(4);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(stackInSlot, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockEntity blockEntity = player.level().getBlockEntity(blockPos);
        return blockEntity instanceof CoalGeneratorBlockEntity
                && player.distanceToSqr(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) <= 64.0;
    }
}
