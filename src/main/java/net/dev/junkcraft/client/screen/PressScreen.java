package net.dev.junkcraft.client.screen;

import net.dev.junkcraft.JunkCraft;
import net.dev.junkcraft.menu.PressMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PressScreen extends AbstractContainerScreen<PressMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(JunkCraft.MODID, "textures/gui/press.png");

    private static final int ENERGY_BAR_X = 151;
    private static final int ENERGY_BAR_Y = 18;
    private static final int ENERGY_BAR_WIDTH = 8;
    private static final int ENERGY_BAR_HEIGHT = 54;

    private static final int ARROW_X = 77;
    private static final int ARROW_Y = 41;
    private static final int ARROW_WIDTH = 26;
    private static final int ARROW_HEIGHT = 14;

    public PressScreen(PressMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        int maxProgress = this.menu.getMaxProgress();
        if (maxProgress > 0) {
            int scaled = Math.round(this.menu.getProgress() * (float) ARROW_WIDTH / maxProgress);
            guiGraphics.fill(x + ARROW_X, y + ARROW_Y, x + ARROW_X + scaled, y + ARROW_Y + ARROW_HEIGHT, 0xFFAA55EE);
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

        String energyText = this.menu.getEnergyStored() + " / " + this.menu.getMaxEnergyStored() + " RF";
        guiGraphics.drawString(this.font, energyText, 10, 15, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
