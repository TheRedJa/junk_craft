package net.dev.junkcraft.jei.recipe;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.dev.junkcraft.JunkCraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Shows how a vanilla Furnace becomes a Coal Generator via the upgrade item. */
public class FurnaceUpgradeCategory implements IRecipeCategory<FurnaceUpgradeRecipe> {
    public static final RecipeType<FurnaceUpgradeRecipe> TYPE =
            RecipeType.create(JunkCraft.MODID, "furnace_upgrade", FurnaceUpgradeRecipe.class);

    private static final int WIDTH = 130;
    private static final int HEIGHT = 46;

    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable plus;

    public FurnaceUpgradeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(JunkCraft.COAL_GENERATOR_UPGRADE.get()));
        this.arrow = guiHelper.getRecipeArrow();
        this.plus = guiHelper.getRecipePlusSign();
    }

    @Override
    public RecipeType<FurnaceUpgradeRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.junkcraft.furnace_upgrade");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FurnaceUpgradeRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(4, 15).addItemStack(new ItemStack(Items.FURNACE));
        builder.addInputSlot(30, 15).addItemStack(new ItemStack(JunkCraft.COAL_GENERATOR_UPGRADE.get()));
        builder.addOutputSlot(106, 15).addItemStack(new ItemStack(JunkCraft.COAL_GENERATOR_ITEM.get()));
    }

    @Override
    public void draw(FurnaceUpgradeRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        plus.draw(guiGraphics, 22, 16);
        arrow.draw(guiGraphics, 62, 15);
    }
}
