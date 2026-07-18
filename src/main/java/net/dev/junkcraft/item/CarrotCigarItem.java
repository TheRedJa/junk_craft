package net.dev.junkcraft.item;

import java.util.List;

import net.dev.junkcraft.mood.Mood;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * A carrot cigar: unlit and inert until sneak-right-clicked while holding
 * Flint and Steel in the offhand. Once lit it glows and can be "sucked" on
 * (right-clicked) 5 times, each puff spawning smoke, before it's used up.
 */
public class CarrotCigarItem extends Item {
    private static final int MAX_PUFFS = 5;
    private static final int USE_DURATION = 20;

    public CarrotCigarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        boolean lit = stack.getOrDefault(ModDataComponents.CIGAR_LIT.get(), false);

        if (!lit && player.isShiftKeyDown() && player.getOffhandItem().is(Items.FLINT_AND_STEEL)) {
            if (!level.isClientSide) {
                stack.set(ModDataComponents.CIGAR_LIT.get(), true);
                stack.set(ModDataComponents.CIGAR_PUFFS_LEFT.get(), MAX_PUFFS);
                player.getOffhandItem().hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
                level.playSound(null, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS,
                        1.0F, level.random.nextFloat() * 0.4F + 0.8F);
            } else {
                for (int i = 0; i < 8; i++) {
                    double x = player.getX() + (level.random.nextDouble() - 0.5) * 0.3;
                    double y = player.getY() + player.getEyeHeight() - 0.2 + (level.random.nextDouble() - 0.5) * 0.2;
                    double z = player.getZ() + (level.random.nextDouble() - 0.5) * 0.3;
                    level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.02, 0.0);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        int puffsLeft = stack.getOrDefault(ModDataComponents.CIGAR_PUFFS_LEFT.get(), MAX_PUFFS);
        if (lit && !player.isShiftKeyDown() && puffsLeft > 0) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide) {
            double x = livingEntity.getX() + (level.random.nextDouble() - 0.5) * 0.3;
            double y = livingEntity.getY() + livingEntity.getEyeHeight() + (level.random.nextDouble() - 0.5) * 0.2;
            double z = livingEntity.getZ() + (level.random.nextDouble() - 0.5) * 0.3;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.03, 0.0);
            if (level.random.nextFloat() < 0.4F) {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.1, z, 0.0, 0.02, 0.0);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide) {
            int puffsLeft = stack.getOrDefault(ModDataComponents.CIGAR_PUFFS_LEFT.get(), MAX_PUFFS) - 1;
            if (livingEntity instanceof Player player) {
                Mood.add(player, 8);
            }
            if (puffsLeft <= 0) {
                level.playSound(null, livingEntity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6F, 1.4F);
                stack.shrink(1);
            } else {
                stack.set(ModDataComponents.CIGAR_PUFFS_LEFT.get(), puffsLeft);
            }
        }
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide && isSelected && stack.getOrDefault(ModDataComponents.CIGAR_LIT.get(), false)
                && level.random.nextFloat() < 0.06F) {
            double x = entity.getX() + (level.random.nextDouble() - 0.5) * 0.4;
            double y = entity.getY() + entity.getBbHeight() * 0.8;
            double z = entity.getZ() + (level.random.nextDouble() - 0.5) * 0.4;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.02, 0.0);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.CIGAR_LIT.get(), false);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.CIGAR_LIT.get(), false);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int puffsLeft = stack.getOrDefault(ModDataComponents.CIGAR_PUFFS_LEFT.get(), MAX_PUFFS);
        return Math.round(13.0F * puffsLeft / MAX_PUFFS);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FF00;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        boolean lit = stack.getOrDefault(ModDataComponents.CIGAR_LIT.get(), false);
        if (lit) {
            int puffsLeft = stack.getOrDefault(ModDataComponents.CIGAR_PUFFS_LEFT.get(), MAX_PUFFS);
            tooltipComponents.add(Component.translatable("item.junkcraft.carrot_cigar.lit", puffsLeft).withStyle(ChatFormatting.GOLD));
        } else {
            tooltipComponents.add(Component.translatable("item.junkcraft.carrot_cigar.unlit").withStyle(ChatFormatting.GRAY));
        }
    }
}
