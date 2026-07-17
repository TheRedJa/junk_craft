package net.dev.junkcraft.sound;

import net.dev.junkcraft.JunkCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, JunkCraft.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> PERFECT_FART = SOUND_EVENTS.register("perfect_fart",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(JunkCraft.MODID, "perfect_fart")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ROMANCE = SOUND_EVENTS.register("romance",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(JunkCraft.MODID, "romance")));

    public static final DeferredHolder<SoundEvent, SoundEvent> VILLAGER_BREED_FAIL = SOUND_EVENTS.register("villager_breed_fail",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(JunkCraft.MODID, "villager_breed_fail")));

    public static final DeferredHolder<SoundEvent, SoundEvent> INTRO = SOUND_EVENTS.register("intro",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(JunkCraft.MODID, "intro")));
}
