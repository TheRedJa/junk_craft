package net.dev.junkcraft.menu;

import net.dev.junkcraft.JunkCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, JunkCraft.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<CoalGeneratorMenu>> COAL_GENERATOR =
            MENU_TYPES.register("coal_generator", () -> IMenuTypeExtension.create(
                    (windowId, inv, data) -> new CoalGeneratorMenu(windowId, inv, data.readBlockPos())));

    public static final DeferredHolder<MenuType<?>, MenuType<PressMenu>> PRESS =
            MENU_TYPES.register("press", () -> IMenuTypeExtension.create(
                    (windowId, inv, data) -> new PressMenu(windowId, inv, data.readBlockPos())));
}
