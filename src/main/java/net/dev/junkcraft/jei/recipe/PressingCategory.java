package net.dev.junkcraft.jei.recipe;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.dev.junkcraft.JunkCraft;
import net.dev.junkcraft.recipe.PressRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Shows the Press block's crushing/pressing recipes, driven directly by {@link PressRecipe}. */
public class PressingCategory implements IRecipeCategory<PressRecipe> {
    public static final RecipeType<PressRecipe> TYPE = RecipeType.create(JunkCraft.MODID, "pressing", PressRecipe.class);

    private static final int WIDTH = 130;
    private static final int HEIGHT = 56;

    private final IDrawable icon;
    private final IDrawable arrow;

    public PressingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(JunkCraft.PRESS_ITEM.get()));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<PressRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.junkcraft.pressing");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PressRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(4, 4).addItemStack(new ItemStack(recipe.primary(), recipe.primaryCount()));
        if (recipe.secondary() != null) {
            builder.addInputSlot(4, 26).addItemStack(new ItemStack(recipe.secondary(), recipe.secondaryCount()));
        }
        builder.addOutputSlot(80, 15).addItemStack(recipe.result());
    }

    @Override
    public void draw(PressRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 42, 15);

        Font font = net.minecraft.client.Minecraft.getInstance().font;
        float seconds = recipe.processTicks() / 20.0F;
        Component rfText = Component.literal(recipe.totalRfCost() + " RF").withStyle(ChatFormatting.GRAY);
        Component timeText = Component.literal(seconds + "s").withStyle(ChatFormatting.GRAY);
        guiGraphics.drawString(font, rfText, 4, HEIGHT - 20, 0x555555, false);
        guiGraphics.drawString(font, timeText, 4, HEIGHT - 10, 0x555555, false);
    }
}
