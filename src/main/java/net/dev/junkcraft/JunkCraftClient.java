package net.dev.junkcraft;

import net.dev.junkcraft.client.screen.CoalGeneratorScreen;
import net.dev.junkcraft.client.screen.PressScreen;
import net.dev.junkcraft.menu.ModMenuTypes;
import net.dev.junkcraft.mood.ClientMoodCache;
import net.dev.junkcraft.mood.Mood;
import net.dev.junkcraft.sound.ModSounds;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = JunkCraft.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = JunkCraft.MODID, value = Dist.CLIENT)
public class JunkCraftClient {
    public JunkCraftClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        JunkCraft.LOGGER.info("HELLO FROM CLIENT SETUP");
        JunkCraft.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.COAL_GENERATOR.get(), CoalGeneratorScreen::new);
        event.register(ModMenuTypes.PRESS.get(), PressScreen::new);
    }

    // Play the intro jingle once on the very first main menu of the session, and fade the menu in
    // from black over the course of the jingle instead of showing it immediately.
    private static final long INTRO_FADE_MS = 8200L;
    private static boolean introPlayed = false;
    private static long introStartMs = -1L;

    @SubscribeEvent
    static void onTitleScreenInit(ScreenEvent.Init.Post event) {
        if (introPlayed || !(event.getScreen() instanceof TitleScreen)) {
            return;
        }
        introPlayed = true;
        introStartMs = Util.getMillis();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.INTRO.get(), 1.0F, 1.0F));
    }

    @SubscribeEvent
    static void onTitleScreenRender(ScreenEvent.Render.Post event) {
        if (introStartMs < 0 || !(event.getScreen() instanceof TitleScreen screen)) {
            return;
        }
        long elapsed = Util.getMillis() - introStartMs;
        if (elapsed >= INTRO_FADE_MS) {
            introStartMs = -1L;
            return;
        }
        float remaining = 1.0F - (float) elapsed / INTRO_FADE_MS;
        int alpha = Math.round(remaining * 255.0F);
        int color = (alpha << 24);
        event.getGuiGraphics().fill(0, 0, screen.width, screen.height, color);
    }

    // Draw the Mood bar one row above where the hunger bar sits, in the same spot regardless of
    // gamemode (the hunger bar itself is hidden in Creative/Spectator, but Mood should still show).
    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 5;
    private static final int BAR_Y_OFFSET_ABOVE_HUNGER = 49;

    @SubscribeEvent
    static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || player.isSpectator() || minecraft.options.hideGui) {
            return;
        }

        var guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int right = screenWidth / 2 + 91;
        int left = right - BAR_WIDTH;
        int top = screenHeight - BAR_Y_OFFSET_ABOVE_HUNGER;

        int mood = ClientMoodCache.get();
        float fraction = Mth.clamp(mood, Mood.MIN, Mood.MAX) / (float) Mood.MAX;
        int filledWidth = Math.round(BAR_WIDTH * fraction);
        int fillColor = Mth.hsvToRgb(fraction / 3.0F, 1.0F, 1.0F) | 0xFF000000;

        guiGraphics.fill(left - 1, top - 1, right + 1, top + BAR_HEIGHT + 1, 0x80000000);
        guiGraphics.fill(left, top, right, top + BAR_HEIGHT, 0xFF3A3A3A);
        guiGraphics.fill(left, top, left + filledWidth, top + BAR_HEIGHT, fillColor);

        guiGraphics.drawString(minecraft.font, "Mood " + mood, left, top - 10, 0xFFFFFF, true);
    }
}
