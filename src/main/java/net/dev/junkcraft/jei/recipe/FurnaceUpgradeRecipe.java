package net.dev.junkcraft.jei.recipe;

/** Marker instance for the single "furnace + upgrade -> coal generator" transform shown in JEI. */
public record FurnaceUpgradeRecipe() {
    public static final FurnaceUpgradeRecipe INSTANCE = new FurnaceUpgradeRecipe();
}
