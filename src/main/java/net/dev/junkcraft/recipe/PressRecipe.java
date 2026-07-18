package net.dev.junkcraft.recipe;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A hardcoded recipe for the Press block: consumes a primary ingredient and an
 * optional secondary ingredient, producing a result over {@link #processTicks} ticks
 * at {@link #RF_PER_TICK} RF per tick.
 */
public record PressRecipe(Item primary, int primaryCount, @Nullable Item secondary, int secondaryCount, ItemStack result, int processTicks) {
    public static final int RF_PER_TICK = 40;

    public boolean matches(ItemStack primaryStack, ItemStack secondaryStack) {
        if (!primaryStack.is(primary) || primaryStack.getCount() < primaryCount) {
            return false;
        }
        if (secondary == null) {
            return true;
        }
        return secondaryStack.is(secondary) && secondaryStack.getCount() >= secondaryCount;
    }

    public int totalRfCost() {
        return processTicks * RF_PER_TICK;
    }
}
