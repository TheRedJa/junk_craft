package net.dev.junkcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Super bonemeal: repeatedly bonemeals the targeted plant so it grows to
 * maturity (or spreads several times) in a single use, instead of the usual
 * random partial growth per application.
 */
public class KakaItem extends Item {
    private static final int MAX_APPLICATIONS = 16;

    public KakaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof BonemealableBlock bonemealable) || !bonemealable.isValidBonemealTarget(level, pos, state)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        boolean grew = false;
        for (int i = 0; i < MAX_APPLICATIONS; i++) {
            BlockState currentState = level.getBlockState(pos);
            if (!(currentState.getBlock() instanceof BonemealableBlock currentBonemealable)
                    || !currentBonemealable.isValidBonemealTarget(level, pos, currentState)) {
                break;
            }
            currentBonemealable.performBonemeal(serverLevel, serverLevel.getRandom(), pos, currentState);
            grew = true;
        }

        if (!grew) {
            return InteractionResult.PASS;
        }

        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        level.levelEvent(1505, pos, 15);
        if (context.getPlayer() != null) {
            context.getPlayer().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        }
        return InteractionResult.CONSUME;
    }
}
