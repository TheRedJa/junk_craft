package net.dev.junkcraft;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.dev.junkcraft.block.CoalGeneratorBlock;
import net.dev.junkcraft.block.entity.ModBlockEntities;
import net.dev.junkcraft.item.CoalGeneratorUpgradeItem;
import net.dev.junkcraft.item.FunPipeItem;
import net.dev.junkcraft.item.KakaItem;
import net.dev.junkcraft.item.MagicNukkelFlascheItem;
import net.dev.junkcraft.menu.ModMenuTypes;
import net.dev.junkcraft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(JunkCraft.MODID)
public class JunkCraft {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "junkcraft";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "junkcraft" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "junkcraft" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "junkcraft" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "junkcraft:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "junkcraft:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Creates a new food item with the id "junkcraft:example_item", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // The Coal Generator: created by right-clicking a vanilla furnace with the Coal Generator Upgrade item
    public static final DeferredBlock<CoalGeneratorBlock> COAL_GENERATOR = BLOCKS.register("coal_generator",
            () -> new CoalGeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(CoalGeneratorBlock.LIT) ? 13 : 0)));
    public static final DeferredItem<BlockItem> COAL_GENERATOR_ITEM = ITEMS.registerSimpleBlockItem("coal_generator", COAL_GENERATOR);

    // The upgrade item used to transform a vanilla furnace into a Coal Generator
    public static final DeferredItem<Item> COAL_GENERATOR_UPGRADE = ITEMS.register("coal_generator_upgrade",
            () -> new CoalGeneratorUpgradeItem(new Item.Properties()));

    // Fun Pipe - held like a flute, smoke particles, makes you "high"
    public static final DeferredItem<Item> FUN_PIPE = ITEMS.register("fun_pipe",
            () -> new FunPipeItem(new Item.Properties().stacksTo(1)
                    .food(new FoodProperties.Builder()
                            .alwaysEdible()
                            .nutrition(0)
                            .saturationModifier(0f)
                            .build())));

    // Magic Nukkel Flasche - gives nausea and flight when consumed
    public static final DeferredItem<Item> MAGIC_NUKKEL_FLASCHE = ITEMS.register("magic_nukkel_flasche",
            () -> new MagicNukkelFlascheItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .alwaysEdible()
                            .nutrition(2)
                            .saturationModifier(0.1f)
                            .build())));

    // Kaka - dropped when a player sneaks; also works as a super bonemeal that grows plants almost instantly
    public static final DeferredItem<Item> KAKA = ITEMS.register("kaka", () -> new KakaItem(new Item.Properties()));

    // Creates a creative tab with the id "junkcraft:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.junkcraft")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(FUN_PIPE.get());
                output.accept(MAGIC_NUKKEL_FLASCHE.get());
                output.accept(COAL_GENERATOR_UPGRADE.get());
                output.accept(COAL_GENERATOR_ITEM.get());
                output.accept(KAKA.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public JunkCraft(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so block entity types get registered
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so menu types get registered
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so sound events get registered
        ModSounds.SOUND_EVENTS.register(modEventBus);
        // Register energy/fluid capabilities for the Coal Generator
        modEventBus.addListener(this::registerCapabilities);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (JunkCraft) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.COAL_GENERATOR.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.COAL_GENERATOR.get(),
                (blockEntity, side) -> blockEntity.getFluidTank());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.COAL_GENERATOR.get(),
                (blockEntity, side) -> blockEntity.getFuelItems());
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // Play a romance sound when two animals actually breed (not just when one is fed into love mode)
    @SubscribeEvent
    public void onBabyEntitySpawn(BabyEntitySpawnEvent event) {
        Mob parentA = event.getParentA();
        if (parentA.level().isClientSide) return;

        BlockPos pos = parentA.blockPosition();
        parentA.level().playSound(null, pos, ModSounds.ROMANCE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    // Villagers don't fire BabyEntitySpawnEvent, so track their courting brain memory directly:
    // play the romance sound as soon as a pair starts courting (which is also when heart particles begin),
    // and play a fail sound if the courting ends without a new baby villager showing up (e.g. no free bed).
    private static final Map<UUID, Set<UUID>> VILLAGER_COURTING_SNAPSHOT = new HashMap<>();

    @SubscribeEvent
    public void onVillagerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.level().isClientSide) return;

        boolean courting = villager.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
        boolean wasCourting = villager.getPersistentData().getBoolean("junkcraft.villager_courting");

        if (courting && !wasCourting) {
            villager.level().playSound(null, villager.blockPosition(), ModSounds.ROMANCE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            VILLAGER_COURTING_SNAPSHOT.put(villager.getUUID(), nearbyBabyVillagerIds(villager));
        } else if (!courting && wasCourting) {
            Set<UUID> before = VILLAGER_COURTING_SNAPSHOT.remove(villager.getUUID());
            Set<UUID> after = nearbyBabyVillagerIds(villager);
            boolean gotNewBaby = before != null && after.stream().anyMatch(id -> !before.contains(id));
            if (!gotNewBaby) {
                villager.level().playSound(null, villager.blockPosition(), ModSounds.VILLAGER_BREED_FAIL.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        }

        villager.getPersistentData().putBoolean("junkcraft.villager_courting", courting);
    }

    private static Set<UUID> nearbyBabyVillagerIds(Villager villager) {
        return villager.level()
                .getEntitiesOfClass(Villager.class, villager.getBoundingBox().inflate(4.0), Villager::isBaby)
                .stream()
                .map(Entity::getUUID)
                .collect(Collectors.toSet());
    }

    // Handle flight removal after the duration expires, and drop a Kaka with a fart sound whenever a player starts sneaking
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        if (player.level().isClientSide) return;
        long flightEnd = player.getPersistentData().getLong("junkcraft.flight_end");
        if (flightEnd > 0 && player.level().getGameTime() >= flightEnd) {
            if (player.getAbilities().mayfly && !player.getAbilities().instabuild) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            player.getPersistentData().remove("junkcraft.flight_end");
        }

        boolean isSneaking = player.isShiftKeyDown();
        boolean wasSneaking = player.getPersistentData().getBoolean("junkcraft.was_sneaking");
        if (isSneaking && !wasSneaking) {
            player.spawnAtLocation(new ItemStack(KAKA.get()));
            player.level().playSound(null, player.blockPosition(), ModSounds.PERFECT_FART.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.getPersistentData().putBoolean("junkcraft.was_sneaking", isSneaking);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
