package net.dev.junkcraft.jei.recipe;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.dev.junkcraft.JunkCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Shows one fuel item (or lava) burning in the Coal Generator, and the RF it produces. */
public class FuelCategory implements IRecipeCategory<FuelEntry> {
    public static final RecipeType<FuelEntry> TYPE = RecipeType.create(JunkCraft.MODID, "coal_generator_fuel", FuelEntry.class);

    private static final int WIDTH = 130;
    private static final int HEIGHT = 30;

    private final IDrawable icon;
    private final IDrawable flame;

    public FuelCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(JunkCraft.COAL_GENERATOR_ITEM.get()));
        this.flame = guiHelper.getRecipeFlameFilled();
    }

    @Override
    public RecipeType<FuelEntry> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.junkcraft.coal_generator_fuel");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FuelEntry recipe, IFocusGroup focuses) {
        builder.addInputSlot(4, 7).addItemStack(recipe.icon());
    }

    @Override
    public void draw(FuelEntry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        flame.draw(guiGraphics, 26, 7);
        guiGraphics.drawString(Minecraft.getInstance().font, recipe.description(), 46, 12, 0x555555, false);
    }
}
