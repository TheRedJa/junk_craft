package net.dev.junkcraft.menu;

import net.dev.junkcraft.block.entity.PressBlockEntity;
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

public class PressMenu extends AbstractContainerMenu {
    private static final int PRIMARY_SLOT_X = 44;
    private static final int PRIMARY_SLOT_Y = 27;
    private static final int SECONDARY_SLOT_X = 44;
    private static final int SECONDARY_SLOT_Y = 54;
    private static final int OUTPUT_SLOT_X = 117;
    private static final int OUTPUT_SLOT_Y = 41;
    private static final int PLAYER_INV_Y = 84;
    private static final int PLAYER_HOTBAR_Y = 142;
    private static final int OUTPUT_SLOT_INDEX = 2;

    public final BlockPos blockPos;
    public final ContainerData data;

    // Client-side constructor, invoked via ModMenuTypes' IContainerFactory
    public PressMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, pos, new ItemStackHandler(2), new ItemStackHandler(1), new SimpleContainerData(4));
    }

    // Server-side constructor, invoked from PressBlockEntity#createMenu
    public PressMenu(int containerId, Inventory playerInventory, BlockPos pos, IItemHandler inputItems, IItemHandler outputItems, ContainerData data) {
        super(ModMenuTypes.PRESS.get(), containerId);
        this.blockPos = pos;
        this.data = data;

        this.addSlot(new SlotItemHandler(inputItems, 0, PRIMARY_SLOT_X, PRIMARY_SLOT_Y));
        this.addSlot(new SlotItemHandler(inputItems, 1, SECONDARY_SLOT_X, SECONDARY_SLOT_Y));
        this.addSlot(new SlotItemHandler(outputItems, 0, OUTPUT_SLOT_X, OUTPUT_SLOT_Y));

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

    public int getProgress() {
        return data.get(2);
    }

    public int getMaxProgress() {
        return data.get(3);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index <= OUTPUT_SLOT_INDEX) {
                if (!this.moveItemStackTo(stackInSlot, OUTPUT_SLOT_INDEX + 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stackInSlot, 0, OUTPUT_SLOT_INDEX, false)) {
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
        return blockEntity instanceof PressBlockEntity
                && player.distanceToSqr(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) <= 64.0;
    }
}
