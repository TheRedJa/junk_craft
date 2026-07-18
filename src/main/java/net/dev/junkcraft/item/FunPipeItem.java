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

public class FunPipeItem extends Item {
    public FunPipeItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            // "High" effect: nausea + slowness + weakness + mining fatigue for 20 seconds
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 400, 0, false, true));
            Mood.add(player, 15);
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN; // Held like a flute/horn, you blow into it
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && livingEntity instanceof Player player) {
            // Same smoke particles as the magic nukkel flasche
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