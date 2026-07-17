package net.dev.junkcraft.client.screen;

import net.dev.junkcraft.JunkCraft;
import net.dev.junkcraft.menu.CoalGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CoalGeneratorScreen extends AbstractContainerScreen<CoalGeneratorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(JunkCraft.MODID, "textures/gui/coal_generator.png");

    private static final int ENERGY_BAR_X = 151;
    private static final int ENERGY_BAR_Y = 18;
    private static final int ENERGY_BAR_WIDTH = 8;
    private static final int ENERGY_BAR_HEIGHT = 54;

    private static final int FLAME_X = 80;
    private static final int FLAME_Y = 60;
    private static final int FLAME_SIZE = 14;

    public CoalGeneratorScreen(CoalGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        int litDuration = this.menu.getLitDuration();
        if (litDuration > 0) {
            int scaled = Math.round(this.menu.getLitTime() * (float) FLAME_SIZE / litDuration);
            guiGraphics.fill(x + FLAME_X, y + FLAME_Y + (FLAME_SIZE - scaled), x + FLAME_X + FLAME_SIZE, y + FLAME_Y + FLAME_SIZE, 0xFFFF8800);
        }

        int maxEnergy = Math.max(this.menu.getMaxEnergyStored(), 1);
        int filled = Math.round(this.menu.getEnergyStored() * (float) ENERGY_BAR_HEIGHT / maxEnergy);
        guiGraphics.fill(x + ENERGY_BAR_X, y + ENERGY_BAR_Y + (ENERGY_BAR_HEIGHT - filled),
                x + ENERGY_BAR_X + ENERGY_BAR_WIDTH, y + ENERGY_BAR_Y + ENERGY_BAR_HEIGHT, 0xFFFF3333);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        int litTime = this.menu.getLitTime();
        String fuelText = litTime > 0 ? "Fuel: " + ((litTime + 19) / 20) + "s" : "Fuel: -";
        guiGraphics.drawString(this.font, fuelText, 10, 20, 4210752, false);

        String energyText = "Energy: " + this.menu.getEnergyStored() + " / " + this.menu.getMaxEnergyStored() + " RF";
        guiGraphics.drawString(this.font, energyText, 10, 31, 4210752, false);

        String genText = "Gen: " + this.menu.getGenerationRate() + " RF/t";
        guiGraphics.drawString(this.font, genText, 10, 42, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
