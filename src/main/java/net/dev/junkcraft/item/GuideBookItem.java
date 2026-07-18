package net.dev.junkcraft.item;

import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

/**
 * A plain {@link WrittenBookItem} pre-filled with the "Guide to Mood" pages,
 * so it works immediately without needing a lectern or command to write it.
 *
 * <p>{@code use()} is overridden because {@link ServerPlayer#openItemGui} only
 * sends the "open book" packet for the literal vanilla {@code minecraft:written_book}
 * item, not for arbitrary {@link WrittenBookItem} subclasses like this one.
 */
public class GuideBookItem extends WrittenBookItem {
    public GuideBookItem(Properties properties) {
        super(properties);
    }

    public static Properties defaultProperties() {
        return new Properties().stacksTo(1).component(DataComponents.WRITTEN_BOOK_CONTENT, buildContent());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundOpenBookPacket(hand));
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static WrittenBookContent buildContent() {
        List<Filterable<Component>> pages = List.of(
                page("§6§lGuide to Mood§r\n\nEvery adventurer keeps\nscore of how their day\nis going - we call it\nyour §aMood§r (0-100).\n\nWatch the bar above\nyour hunger icons!"),
                page("§2§lWhat cheers you up§r\n\n+8: suck a lit Carrot\nCigar (5 puffs a cigar)\n\n+15: smoke the Fun\nPipe (you'll feel it\nlater...)"),
                page("§2§lMore good vibes§r\n\n+20: chug a Magic\nNukkel Flasche\n\n+5: watch animals or\nvillagers fall in love\nnearby. Aww."),
                page("§4§lWhat brings you down§r\n\n-1: sneeze out a Kaka\nmid-sneak. Awkward.\n\n-5: a villager date\nfails nearby. Cringe."),
                page("§4§lMore bad vibes§r\n\n-1: getting hurt\n\n-15: the comedown once\na Flasche trip ends\n\nBoredom slowly drags a\nhigh Mood back down to\n45, one point every 30\nseconds - never lower."),
                page("§c§lRunning on empty§r\n\nBelow 20 Mood: Weakness\nand Mining Fatigue creep\nin. Misery mines slow.\n\nBut you'll always drift\nback up to 40 on your\nown, one point every 30\nseconds. Go have fun to\nspeed it up!"),
                page("§a§lRiding high§r\n\nAbove 80 Mood:\nRegeneration and Haste\nkick in. Good vibes mine\nfaster and heal quicker.\n\nStay golden."),
                page("§9§lQuick recap§r\n\nMagic Crystal, crushed\nunder a piston or\npressed, becomes a Thing\n\nThing + Bamboo, pressed\n= Fun Pipe\n\nThing + Carrot, pressed\n= Carrot Cigar"),
                page("§9§lLighting up§r\n\nHold the Carrot Cigar,\nFlint and Steel in your\noffhand, then sneak +\nright-click to light it.\n\nRight-click to suck.\n5 puffs and it's gone."),
                page("§6§lA word of warning§r\n\nCarrying a Kaka makes\nyou reek. Mobs within 30\nblocks flee and throw on\na pumpkin mask to cope.\n\nWatch chat: you'll be\ntold when it starts,\nwhen it fades, and get a\nreminder every 15s."));

        return new WrittenBookContent(Filterable.passThrough("Guide to Mood"), "Junk Craft", 0, pages, true);
    }

    private static Filterable<Component> page(String text) {
        return Filterable.passThrough(Component.literal(text));
    }
}
