package net.dev.junkcraft.block.entity;

import net.dev.junkcraft.JunkCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, JunkCraft.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoalGeneratorBlockEntity>> COAL_GENERATOR =
            BLOCK_ENTITY_TYPES.register("coal_generator", () -> BlockEntityType.Builder.of(
                    CoalGeneratorBlockEntity::new, JunkCraft.COAL_GENERATOR.get()).build(null));
}
