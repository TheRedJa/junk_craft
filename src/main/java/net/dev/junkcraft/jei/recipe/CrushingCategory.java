package net.dev.junkcraft.jei.recipe;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.dev.junkcraft.JunkCraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Shows how a piston crushes a Magic Crystal into a Thing. */
public class CrushingCategory implements IRecipeCategory<CrushingRecipe> {
    public static final RecipeType<CrushingRecipe> TYPE =
            RecipeType.create(JunkCraft.MODID, "crushing", CrushingRecipe.class);

    private static final int WIDTH = 100;
    private static final int HEIGHT = 46;

    private final IDrawable icon;
    private final IDrawable arrow;

    public CrushingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.PISTON));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<CrushingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.junkcraft.crushing");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrushingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(4, 15).addItemStack(new ItemStack(Items.PISTON));
        builder.addInputSlot(26, 15).addItemStack(new ItemStack(JunkCraft.MAGIC_CRYSTAL.get()));
        builder.addOutputSlot(76, 15).addItemStack(new ItemStack(JunkCraft.THING.get()));
    }

    @Override
    public void draw(CrushingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 50, 15);
    }
}
