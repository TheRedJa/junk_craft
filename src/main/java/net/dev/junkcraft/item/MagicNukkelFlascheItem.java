package net.dev.junkcraft.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MagicNukkelFlascheItem extends Item {
    public MagicNukkelFlascheItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            // Give nausea for 30 seconds
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 600, 0, false, true));
            // Give flight ability for 30 seconds (600 ticks)
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
            }
            // Schedule removal of flight after 30 seconds
            player.getPersistentData().putLong("junkcraft.flight_end", level.getGameTime() + 600);
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32; // Standard eating time
    }
}