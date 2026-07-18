package net.dev.junkcraft.item;

import net.dev.junkcraft.mood.Mood;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.Random;

/**
 * A cigarette that you hold like a flute (TOOT_HORN animation).
 * Right-click to take a puff — smoke particles appear near your mouth.
 * Each cigarette has 8 puffs before it's used up.
 * Each puff gives a random positive effect and a mood boost.
 */
public class CigaretteItem extends Item {
    private static final int MAX_PUFFS = 8;
    private static final int USE_DURATION = 20;
    private static final Random RANDOM = new Random();

    public CigaretteItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            // Track puffs via damage (durability bar)
            int puffsUsed = stack.getDamageValue();
            puffsUsed++;

            // Give a random positive effect
            applyRandomPositiveEffect(player);

            // Mood boost
            Mood.add(player, 10);

            if (puffsUsed >= MAX_PUFFS) {
                // Cigarette is finished
                stack.shrink(1);
            } else {
                stack.setDamageValue(puffsUsed);
            }
        }
        return stack;
    }

    private void applyRandomPositiveEffect(Player player) {
        // Pick a random positive effect
        int choice = RANDOM.nextInt(8);
        switch (choice) {
            case 0 -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, false, true));
            case 1 -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0, false, true));
            case 2 -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, true));
            case 3 -> player.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, 1, false, true));
            case 4 -> player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1, false, true));
            case 5 -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0, false, true));
            case 6 -> player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, true));
            case 7 -> player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, true));
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN; // Held like a flute — visible in first AND third person
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && livingEntity instanceof Player player) {
            // Spawn smoke particles near the player's mouth
            // The player's look direction determines where the smoke goes
            double lookX = player.getLookAngle().x;
            double lookY = player.getLookAngle().y;
            double lookZ = player.getLookAngle().z;

            // Mouth position: slightly in front of the face
            double mouthX = player.getX() + lookX * 0.4;
            double mouthY = player.getY() + player.getEyeHeight() - 0.1 + lookY * 0.4;
            double mouthZ = player.getZ() + lookZ * 0.4;

            // Main smoke puff
            level.addParticle(ParticleTypes.SMOKE,
                    mouthX + (level.random.nextDouble() - 0.5) * 0.1,
                    mouthY + (level.random.nextDouble() - 0.5) * 0.1,
                    mouthZ + (level.random.nextDouble() - 0.5) * 0.1,
                    lookX * 0.05 + (level.random.nextDouble() - 0.5) * 0.02,
                    lookY * 0.05 + 0.02,
                    lookZ * 0.05 + (level.random.nextDouble() - 0.5) * 0.02);

            // Occasional cozy campfire smoke (lazier, bigger)
            if (level.random.nextFloat() < 0.4F) {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        mouthX + (level.random.nextDouble() - 0.5) * 0.15,
                        mouthY + 0.1 + (level.random.nextDouble() - 0.5) * 0.1,
                        mouthZ + (level.random.nextDouble() - 0.5) * 0.15,
                        lookX * 0.03,
                        0.04,
                        lookZ * 0.03);
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getDamageValue() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int puffsUsed = stack.getDamageValue();
        int puffsLeft = MAX_PUFFS - puffsUsed;
        return Math.round(13.0F * puffsLeft / MAX_PUFFS);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xCCCCCC; // Light grey
    }
}