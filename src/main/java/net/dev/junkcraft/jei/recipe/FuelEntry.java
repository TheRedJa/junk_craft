package net.dev.junkcraft.jei.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** One "fuel -> RF" entry for the Coal Generator's fuel category. */
public record FuelEntry(ItemStack icon, Component description) {
}
