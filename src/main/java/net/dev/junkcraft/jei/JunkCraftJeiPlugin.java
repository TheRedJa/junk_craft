package net.dev.junkcraft.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.dev.junkcraft.JunkCraft;
import net.dev.junkcraft.jei.recipe.CrushingCategory;
import net.dev.junkcraft.jei.recipe.CrushingRecipe;
import net.dev.junkcraft.jei.recipe.FuelCategory;
import net.dev.junkcraft.jei.recipe.FuelEntry;
import net.dev.junkcraft.jei.recipe.FurnaceUpgradeCategory;
import net.dev.junkcraft.jei.recipe.FurnaceUpgradeRecipe;
import net.dev.junkcraft.jei.recipe.PressingCategory;
import net.dev.junkcraft.recipe.PressRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@JeiPlugin
public class JunkCraftJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(JunkCraft.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new FurnaceUpgradeCategory(guiHelper),
                new CrushingCategory(guiHelper),
                new PressingCategory(guiHelper),
                new FuelCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(FurnaceUpgradeCategory.TYPE, List.of(FurnaceUpgradeRecipe.INSTANCE));
        registration.addRecipes(CrushingCategory.TYPE, List.of(CrushingRecipe.INSTANCE));
        registration.addRecipes(PressingCategory.TYPE, PressRecipes.all());
        registration.addRecipes(FuelCategory.TYPE, buildFuelEntries());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.FURNACE), FurnaceUpgradeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(JunkCraft.COAL_GENERATOR_UPGRADE.get()), FurnaceUpgradeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(Items.PISTON), CrushingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.STICKY_PISTON), CrushingCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(JunkCraft.PRESS_ITEM.get()), PressingCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(JunkCraft.COAL_GENERATOR_ITEM.get()), FuelCategory.TYPE);
    }

    private static List<FuelEntry> buildFuelEntries() {
        List<FuelEntry> entries = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            int burnTicks = stack.getBurnTime(null);
            if (burnTicks <= 0 || item instanceof BucketItem) {
                continue;
            }
            int rf = burnTicks * net.dev.junkcraft.block.entity.CoalGeneratorBlockEntity.RF_PER_TICK;
            entries.add(new FuelEntry(stack, Component.literal(rf + " RF").withStyle(ChatFormatting.GRAY)));
        }

        entries.add(new FuelEntry(new ItemStack(Items.LAVA_BUCKET),
                Component.literal("40 RF per 5 mB (from pipes)").withStyle(ChatFormatting.GRAY)));

        return entries;
    }
}
