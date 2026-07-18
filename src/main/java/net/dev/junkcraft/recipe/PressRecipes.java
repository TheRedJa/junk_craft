package net.dev.junkcraft.recipe;

import java.util.List;

import net.dev.junkcraft.JunkCraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PressRecipes {
    // Built lazily (not a static final list) so this class can be loaded any time
    // without depending on JunkCraft's DeferredItems already being resolved.
    public static List<PressRecipe> all() {
        return List.of(
                new PressRecipe(JunkCraft.MAGIC_CRYSTAL.get(), 1, null, 0, new ItemStack(JunkCraft.THING.get()), 80),
                new PressRecipe(JunkCraft.THING.get(), 1, Items.BAMBOO, 1, new ItemStack(JunkCraft.FUN_PIPE.get()), 100),
                new PressRecipe(JunkCraft.THING.get(), 1, Items.CARROT, 1, new ItemStack(JunkCraft.CARROT_CIGAR.get()), 100));
    }

    private PressRecipes() {}
}
