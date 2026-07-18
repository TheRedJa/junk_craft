package net.dev.junkcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.joml.Vector3f;

import com.mojang.logging.LogUtils;

import net.dev.junkcraft.block.CoalGeneratorBlock;
import net.dev.junkcraft.block.PressBlock;
import net.dev.junkcraft.block.entity.ModBlockEntities;
import net.dev.junkcraft.item.CarrotCigarItem;
import net.dev.junkcraft.item.CoalGeneratorUpgradeItem;
import net.dev.junkcraft.item.FunPipeItem;
import net.dev.junkcraft.item.KakaItem;
import net.dev.junkcraft.item.MagicNukkelFlascheItem;
import net.dev.junkcraft.item.GuideBookItem;
import net.dev.junkcraft.item.ModDataComponents;
import net.dev.junkcraft.menu.ModMenuTypes;
import net.dev.junkcraft.mood.Mood;
import net.dev.junkcraft.network.ModNetworking;
import net.dev.junkcraft.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
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
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
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

    // Kaka - dropped when a player sneaks; also works as a super bonemeal that grows plants almost instantly.
    // Also, regrettably, edible - gives Nausea and 5 seconds of very public vomiting
    public static final DeferredItem<Item> KAKA = ITEMS.register("kaka",
            () -> new KakaItem(new Item.Properties().food(new FoodProperties.Builder()
                    .alwaysEdible()
                    .nutrition(1)
                    .saturationModifier(0f)
                    .build())));

    // Magic Crystal - crush it under a piston (or feed it into a Press) to get a Thing
    public static final DeferredItem<Item> MAGIC_CRYSTAL = ITEMS.registerSimpleItem("magic_crystal", new Item.Properties());

    // Thing - made by crushing a Magic Crystal; feed it plus bamboo into a Press to get a Fun Pipe
    public static final DeferredItem<Item> THING = ITEMS.registerSimpleItem("thing", new Item.Properties());

    // The Press: an RF-powered machine that crushes Magic Crystal into Thing, and presses Thing + Bamboo into a Fun Pipe
    public static final DeferredBlock<PressBlock> PRESS = BLOCKS.register("press",
            () -> new PressBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));
    public static final DeferredItem<BlockItem> PRESS_ITEM = ITEMS.registerSimpleBlockItem("press", PRESS);

    // Carrot Cigar - press a Thing with a Carrot, then light it (sneak + right-click with Flint and Steel
    // in your offhand) and suck on it (right-click) up to 5 times before it's used up
    public static final DeferredItem<Item> CARROT_CIGAR = ITEMS.register("carrot_cigar",
            () -> new CarrotCigarItem(new Item.Properties().stacksTo(1)));

    // Guide to Mood - a written book explaining the Mood stat, handed out on first join
    public static final DeferredItem<Item> GUIDE_BOOK = ITEMS.register("guide_book",
            () -> new GuideBookItem(GuideBookItem.defaultProperties()));

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
                output.accept(MAGIC_CRYSTAL.get());
                output.accept(THING.get());
                output.accept(PRESS_ITEM.get());
                output.accept(CARROT_CIGAR.get());
                output.accept(GUIDE_BOOK.get());
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
        // Register the Deferred Register to the mod event bus so data component types get registered
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        // Register energy/fluid capabilities for the Coal Generator
        modEventBus.addListener(this::registerCapabilities);
        // Register the Mood sync network payload
        modEventBus.addListener(ModNetworking::register);

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

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.PRESS.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.PRESS.get(),
                (blockEntity, side) -> blockEntity.getCombinedItemHandler());
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // Crush a Magic Crystal item into a Thing when a piston pushes a block onto it
    @SubscribeEvent
    public void onPistonMoved(PistonEvent.Post event) {
        if (!event.getPistonMoveType().isExtend) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos crushPos = event.getFaceOffsetPos();
        AABB crushBox = new AABB(crushPos);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, crushBox, e -> e.getItem().is(MAGIC_CRYSTAL.get()))) {
            ItemStack crystalStack = itemEntity.getItem();
            itemEntity.setItem(new ItemStack(THING.get(), crystalStack.getCount()));
        }
    }

    // Play a romance sound when two animals actually breed (not just when one is fed into love mode),
    // and cheer up any nearby players who get to watch
    @SubscribeEvent
    public void onBabyEntitySpawn(BabyEntitySpawnEvent event) {
        Mob parentA = event.getParentA();
        if (parentA.level().isClientSide) return;

        BlockPos pos = parentA.blockPosition();
        parentA.level().playSound(null, pos, ModSounds.ROMANCE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        changeMoodNearby(parentA.level(), pos, ROMANCE_MOOD_RADIUS, ROMANCE_MOOD_BONUS);
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
            changeMoodNearby(villager.level(), villager.blockPosition(), ROMANCE_MOOD_RADIUS, ROMANCE_MOOD_BONUS);
            VILLAGER_COURTING_SNAPSHOT.put(villager.getUUID(), nearbyBabyVillagerIds(villager));
        } else if (!courting && wasCourting) {
            Set<UUID> before = VILLAGER_COURTING_SNAPSHOT.remove(villager.getUUID());
            Set<UUID> after = nearbyBabyVillagerIds(villager);
            boolean gotNewBaby = before != null && after.stream().anyMatch(id -> !before.contains(id));
            if (!gotNewBaby) {
                villager.level().playSound(null, villager.blockPosition(), ModSounds.VILLAGER_BREED_FAIL.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                changeMoodNearby(villager.level(), villager.blockPosition(), ROMANCE_MOOD_RADIUS, VILLAGER_FAIL_MOOD_PENALTY);
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

    private static final double ROMANCE_MOOD_RADIUS = 16.0;
    private static final int ROMANCE_MOOD_BONUS = 5;
    private static final int VILLAGER_FAIL_MOOD_PENALTY = -5;

    private static void changeMoodNearby(net.minecraft.world.level.Level level, BlockPos pos, double radius, int delta) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        AABB box = AABB.ofSize(pos.getCenter(), radius * 2, radius * 2, radius * 2);
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, box)) {
            Mood.add(player, delta);
        }
    }

    // Carrying a Kaka around makes every mob within 30 blocks flee from the smell and, if
    // bare-headed, throw on a mask (a carved pumpkin) to try and escape it
    private static final double SMELL_RADIUS = 30.0;
    private static final int SMELL_TICK_INTERVAL = 20;
    private static final double FLEE_DISTANCE = 12.0;
    private static final double FLEE_SPEED = 1.3;

    @SubscribeEvent
    public void onMobTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (mob.level().isClientSide) return;
        if (mob.tickCount % SMELL_TICK_INTERVAL != 0) return;

        Player smelly = findNearestKakaHolder(mob);
        boolean wearingMask = mob.getPersistentData().getBoolean("junkcraft.smell_mask");

        if (smelly != null) {
            if (!wearingMask && mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
                mob.getPersistentData().putBoolean("junkcraft.smell_mask", true);
            }

            Vec3 away = mob.position().subtract(smelly.position());
            if (away.lengthSqr() < 1.0E-4) {
                away = new Vec3(mob.getRandom().nextDouble() - 0.5, 0.0, mob.getRandom().nextDouble() - 0.5);
            }
            Vec3 fleeTarget = mob.position().add(away.normalize().scale(FLEE_DISTANCE));
            mob.getNavigation().moveTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, FLEE_SPEED);
        } else if (wearingMask) {
            mob.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            mob.getPersistentData().remove("junkcraft.smell_mask");
        }
    }

    private static Player findNearestKakaHolder(Mob mob) {
        AABB box = mob.getBoundingBox().inflate(SMELL_RADIUS);
        for (Player player : mob.level().getEntitiesOfClass(Player.class, box)) {
            if (hasKaka(player)) {
                return player;
            }
        }
        return null;
    }

    private static boolean hasKaka(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(KAKA.get())) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(KAKA.get())) return true;
        }
        return false;
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
            Mood.add(player, MAGIC_NUKKEL_COMEDOWN_MOOD);
        }

        boolean isSneaking = player.isShiftKeyDown();
        boolean wasSneaking = player.getPersistentData().getBoolean("junkcraft.was_sneaking");
        if (isSneaking && !wasSneaking) {
            player.spawnAtLocation(new ItemStack(KAKA.get()));
            player.level().playSound(null, player.blockPosition(), ModSounds.PERFECT_FART.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            Mood.add(player, KAKA_SNEEZE_MOOD_PENALTY);
        }
        player.getPersistentData().putBoolean("junkcraft.was_sneaking", isSneaking);

        tickVomiting(player);
        tickVomitPuddles(player);
        tickMood(player);
        tickSmell(player);
    }

    // Let the player know why every mob keeps running away from them
    private static final int SMELL_REMINDER_INTERVAL_TICKS = 300; // 15 seconds

    private void tickSmell(Player player) {
        boolean isSmelly = hasKaka(player);
        boolean wasSmelly = player.getPersistentData().getBoolean("junkcraft.was_smelly");

        if (isSmelly && !wasSmelly) {
            player.displayClientMessage(Component.literal(
                    "§2You reek of Kaka! Mobs within 30 blocks will flee from you and cover their faces."), false);
        } else if (!isSmelly && wasSmelly) {
            player.displayClientMessage(Component.literal("§aThe smell has faded. Mobs no longer flee from you."), false);
        } else if (isSmelly && player.level().getGameTime() % SMELL_REMINDER_INTERVAL_TICKS == 0) {
            player.displayClientMessage(Component.literal("§2You still reek of Kaka..."), true);
        }

        player.getPersistentData().putBoolean("junkcraft.was_smelly", isSmelly);
    }

    // While a player is digesting a Kaka, periodically burst vomit particles and a retch sound
    // that every nearby player can see and hear, not just the one throwing up
    private static final int VOMIT_BURST_INTERVAL_TICKS = 15;

    private void tickVomiting(Player player) {
        long vomitEnd = player.getPersistentData().getLong("junkcraft.vomit_end");
        if (vomitEnd <= 0) return;

        long gameTime = player.level().getGameTime();
        if (gameTime >= vomitEnd) {
            player.getPersistentData().remove("junkcraft.vomit_end");
            return;
        }

        if (gameTime % VOMIT_BURST_INTERVAL_TICKS == 0 && player.level() instanceof ServerLevel serverLevel) {
            Vec3 look = player.getLookAngle();
            Vec3 mouth = player.position()
                    .add(0, player.getEyeHeight() - 0.2, 0)
                    .add(look.scale(0.4));
            // Aim the burst along the look direction (angled slightly down) so it reads as spewing
            // out of the mouth instead of a random cloud around the player. sendParticles' offset
            // args are only treated as a fixed velocity vector (not a random spread) when count == 0.
            Vec3 spew = new Vec3(look.x, look.y - 0.4, look.z).normalize();
            RandomSource random = player.getRandom();
            for (int i = 0; i < 5; i++) {
                Vec3 jittered = new Vec3(
                        spew.x + (random.nextDouble() - 0.5) * 0.3,
                        spew.y + (random.nextDouble() - 0.5) * 0.15,
                        spew.z + (random.nextDouble() - 0.5) * 0.3
                );
                serverLevel.sendParticles(ParticleTypes.SNEEZE, mouth.x, mouth.y, mouth.z, 0,
                        jittered.x, jittered.y, jittered.z, 0.35F);
            }
            for (int i = 0; i < 4; i++) {
                Vec3 jittered = new Vec3(
                        spew.x + (random.nextDouble() - 0.5) * 0.3,
                        spew.y + (random.nextDouble() - 0.5) * 0.15,
                        spew.z + (random.nextDouble() - 0.5) * 0.3
                );
                serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, mouth.x, mouth.y, mouth.z, 0,
                        jittered.x, jittered.y, jittered.z, 0.4F);
            }
            serverLevel.playSound(null, player.blockPosition(), ModSounds.VOMIT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    // Kicks off a bout of vomiting: nausea effect, the burst-particle timer above, and a puddle
    // left behind on the ground that lingers and can gross out (or sicken) anyone who finds it.
    public static void startVomiting(ServerLevel level, Player player, int durationTicks) {
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, durationTicks, 0, false, true));
        player.getPersistentData().putLong("junkcraft.vomit_end", level.getGameTime() + durationTicks);
        registerVomitPuddle(level, player.blockPosition());
    }

    // Puddles left behind by vomiting: they sit for a minute, look gross, and make anyone who
    // can see them nauseous - staring straight at one from close range for too long is contagious.
    private static final Map<ResourceKey<Level>, List<VomitPuddle>> VOMIT_PUDDLES = new HashMap<>();
    private static final int VOMIT_PUDDLE_DURATION_TICKS = 1200; // 60 seconds
    private static final int VOMIT_PUDDLE_EFFECT_INTERVAL_TICKS = 10;
    private static final int VOMIT_PUDDLE_NAUSEA_TICKS = 40;
    private static final double VOMIT_PUDDLE_VOMIT_RADIUS = 5.0;
    private static final int VOMIT_PUDDLE_STARE_TICKS = 200; // 10 seconds

    private static final class VomitPuddle {
        final BlockPos pos;
        final long expiryTime;
        long nextEffectTick;

        VomitPuddle(BlockPos pos, long expiryTime) {
            this.pos = pos;
            this.expiryTime = expiryTime;
        }
    }

    private static void registerVomitPuddle(ServerLevel level, BlockPos pos) {
        VOMIT_PUDDLES.computeIfAbsent(level.dimension(), key -> new ArrayList<>())
                .add(new VomitPuddle(pos.immutable(), level.getGameTime() + VOMIT_PUDDLE_DURATION_TICKS));
    }

    private void tickVomitPuddles(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        List<VomitPuddle> puddles = VOMIT_PUDDLES.get(serverLevel.dimension());
        if (puddles == null || puddles.isEmpty()) return;

        long gameTime = serverLevel.getGameTime();
        puddles.removeIf(puddle -> gameTime >= puddle.expiryTime);
        if (puddles.isEmpty()) return;

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookAngle = player.getLookAngle();
        boolean seesAny = false;
        boolean staring = false;

        for (VomitPuddle puddle : puddles) {
            Vec3 groundCenter = Vec3.atBottomCenterOf(puddle.pos).add(0, 0.1, 0);

            // Throttled per-puddle so several players ticking the same puddle in one server
            // tick don't multiply the ambient effects.
            if (gameTime >= puddle.nextEffectTick) {
                puddle.nextEffectTick = gameTime + VOMIT_PUDDLE_EFFECT_INTERVAL_TICKS;
                spawnPuddleEffects(serverLevel, groundCenter);
            }

            // The puddle sits flush with the ground, so raycasting at a single point right on
            // top of it is prone to grazing the block collider and reporting a false miss.
            // Instead treat the whole 3x3x3 area around it as the target - a real bounding-box
            // intersection test that can't be missed by floating-point edge cases.
            AABB bounds = new AABB(puddle.pos).inflate(1.0);
            Vec3 visibilityTarget = bounds.getCenter();

            if (!hasLineOfSight(serverLevel, player, eyePos, visibilityTarget)) continue;
            seesAny = true;

            if (eyePos.distanceToSqr(groundCenter) <= VOMIT_PUDDLE_VOMIT_RADIUS * VOMIT_PUDDLE_VOMIT_RADIUS) {
                Vec3 farPoint = eyePos.add(lookAngle.scale(VOMIT_PUDDLE_VOMIT_RADIUS + 3.0));
                if (bounds.clip(eyePos, farPoint).isPresent()) {
                    staring = true;
                }
            }
        }

        if (seesAny) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, VOMIT_PUDDLE_NAUSEA_TICKS, 0, false, true));
        }

        long stareTicks = player.getPersistentData().getLong("junkcraft.puddle_stare_ticks");
        if (staring) {
            stareTicks++;
            if (stareTicks >= VOMIT_PUDDLE_STARE_TICKS && player.getPersistentData().getLong("junkcraft.vomit_end") <= 0) {
                startVomiting(serverLevel, player, KakaItem.VOMIT_DURATION_TICKS);
                stareTicks = 0;
            }
        } else {
            stareTicks = 0;
        }
        player.getPersistentData().putLong("junkcraft.puddle_stare_ticks", stareTicks);
    }

    private static boolean hasLineOfSight(Level level, Player player, Vec3 from, Vec3 to) {
        ClipContext context = new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
        return level.clip(context).getType() == HitResult.Type.MISS;
    }

    private static void spawnPuddleEffects(ServerLevel level, Vec3 center) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < 10; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 1.6;
            double offsetZ = (random.nextDouble() - 0.5) * 1.6;
            level.sendParticles(new DustParticleOptions(new Vector3f(0.25F, 0.7F, 0.15F), 1.4F),
                    center.x + offsetX, center.y, center.z + offsetZ, 1, 0, 0, 0, 0);
        }
        // Stink rises in a column well above head height so it's clearly visible from a distance,
        // instead of hugging the ground where it's easy to miss.
        for (int i = 0; i < 4; i++) {
            double height = 0.4 + i * 0.6;
            level.sendParticles(ParticleTypes.SNEEZE, center.x, center.y + height, center.z, 3, 0.3, 0.1, 0.3, 0.01);
        }
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 2.4, center.z, 3, 0.3, 0.25, 0.3, 0.02);
    }

    // Mood constants: how the various Junk Craft shenanigans move the Mood stat
    public static final int KAKA_SNEEZE_MOOD_PENALTY = -1;
    public static final int MAGIC_NUKKEL_COMEDOWN_MOOD = -15;
    private static final int MOOD_DRIFT_INTERVAL_TICKS = 600; // 30 seconds
    private static final int BOREDOM_FLOOR = 45;
    private static final int RECOVERY_CEILING = 40;
    private static final int MOOD_EFFECT_CHECK_INTERVAL_TICKS = 100;
    private static final int MOOD_EFFECT_DURATION_TICKS = 200;

    // Boredom slowly drags a high Mood back down to 45 (never lower), and a low Mood slowly
    // recovers back up to 40 (never higher) - both one point every 30 seconds - then apply
    // the low/high Mood consequences
    private void tickMood(Player player) {
        long gameTime = player.level().getGameTime();

        if (gameTime % MOOD_DRIFT_INTERVAL_TICKS == 0) {
            int mood = Mood.get(player);
            if (mood > BOREDOM_FLOOR) {
                Mood.set(player, Math.max(BOREDOM_FLOOR, mood - 1));
            } else if (mood < RECOVERY_CEILING) {
                Mood.set(player, Math.min(RECOVERY_CEILING, mood + 1));
            }
        }

        if (gameTime % MOOD_EFFECT_CHECK_INTERVAL_TICKS == 0) {
            int mood = Mood.get(player);
            if (mood <= Mood.LOW_THRESHOLD) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, MOOD_EFFECT_DURATION_TICKS, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, MOOD_EFFECT_DURATION_TICKS, 0, true, false, true));
            } else if (mood >= Mood.HIGH_THRESHOLD) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, MOOD_EFFECT_DURATION_TICKS, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, MOOD_EFFECT_DURATION_TICKS, 0, true, false, true));
            }
        }
    }

    // Give every player the Guide to Mood once, the first time they join, and sync their current Mood to the HUD
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var player = event.getEntity();
        if (player.level().isClientSide) return;

        Mood.set(player, Mood.get(player));

        if (!player.getPersistentData().getBoolean("junkcraft.received_guide")) {
            player.getPersistentData().putBoolean("junkcraft.received_guide", true);
            player.addItem(new ItemStack(GUIDE_BOOK.get()));
        }
    }

    // Getting hurt puts a damper on your Mood
    @SubscribeEvent
    public void onPlayerDamaged(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide && event.getNewDamage() > 0) {
            Mood.add(player, -1);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
