package net.dev.junkcraft.jei.recipe;

/** Marker instance for the single "piston crushes a Magic Crystal into a Thing" mechanic shown in JEI. */
public record CrushingRecipe() {
    public static final CrushingRecipe INSTANCE = new CrushingRecipe();
}
