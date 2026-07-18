package net.dev.junkcraft.item;

import com.mojang.serialization.Codec;

import net.dev.junkcraft.JunkCraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, JunkCraft.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CIGAR_LIT =
            DATA_COMPONENTS.registerComponentType("cigar_lit",
                    builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CIGAR_PUFFS_LEFT =
            DATA_COMPONENTS.registerComponentType("cigar_puffs_left",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
}
