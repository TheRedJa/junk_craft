package net.dev.junkcraft.item;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.Random;

public class MagicNukkelFlascheItem extends Item {
    private static final String TAG_PUFFS = "junkcraft_puffs";
    private static final int MAX_PUFFS = 5;
    private static final Random RANDOM = new Random();

    @SuppressWarnings("unchecked")
    private static final Holder<MobEffect>[] RANDOM_EFFECTS = new Holder[] {
        MobEffects.CONFUSION,
        MobEffects.POISON,
        MobEffects.WEAKNESS,
        MobEffects.MOVEMENT_SLOWDOWN,
        MobEffects.DIG_SLOWDOWN,
        MobEffects.BLINDNESS,
        MobEffects.HUNGER,
        MobEffects.LEVITATION,
        MobEffects.GLOWING,
        MobEffects.REGENERATION,
        MobEffects.DAMAGE_BOOST,
        MobEffects.MOVEMENT_SPEED,
        MobEffects.JUMP,
        MobEffects.NIGHT_VISION,
        MobEffects.INVISIBILITY,
        MobEffects.FIRE_RESISTANCE,
        MobEffects.WATER_BREATHING,
        MobEffects.DAMAGE_RESISTANCE,
        MobEffects.HEAL,
        MobEffects.DIG_SPEED,
        MobEffects.SATURATION,
        MobEffects.ABSORPTION,
        MobEffects.CONDUIT_POWER,
        MobEffects.DOLPHINS_GRACE,
        MobEffects.HERO_OF_THE_VILLAGE,
        MobEffects.LUCK,
        MobEffects.SLOW_FALLING
    };

    public MagicNukkelFlascheItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            // Get current puff count
            var tag = stack.getComponentsPatch();
            int puffs = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getInt(TAG_PUFFS);
            puffs++;

            // Give a random effect (duration 15 seconds = 300 ticks, amplifier 0-1)
            Holder<MobEffect> randomEffect = RANDOM_EFFECTS[RANDOM.nextInt(RANDOM_EFFECTS.length)];
            int amplifier = RANDOM.nextInt(2); // 0 or 1
            player.addEffect(new MobEffectInstance(randomEffect, 300, amplifier, false, true));

            // Also give flight for 15 seconds
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
            }
            player.getPersistentData().putLong("junkcraft.flight_end", level.getGameTime() + 300);

            if (puffs >= MAX_PUFFS) {
                // Break the flask after 5 puffs
                return ItemStack.EMPTY;
            } else {
                // Update puff count and keep the item
                var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
                var newTag = customData.copyTag();
                newTag.putInt(TAG_PUFFS, puffs);
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(newTag));
                return stack;
            }
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 24; // Slightly faster than eating
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK; // Looks like you're pulling/drinking from it
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && livingEntity instanceof Player player) {
            // Spawn smoke particles around the player's head while using
            double x = player.getX() + (level.random.nextDouble() - 0.5) * 0.5;
            double y = player.getY() + player.getEyeHeight() + (level.random.nextDouble() - 0.5) * 0.3;
            double z = player.getZ() + (level.random.nextDouble() - 0.5) * 0.5;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.02, 0.0);
            if (level.random.nextFloat() < 0.3f) {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.2, z, 0.0, 0.03, 0.0);
            }
        }
    }
}
