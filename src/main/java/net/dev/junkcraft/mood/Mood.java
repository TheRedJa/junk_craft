package net.dev.junkcraft.mood;

import net.dev.junkcraft.network.MoodSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Tracks each player's Mood stat (0-100), stored in their persistent data so it
 * survives death/respawn and world reloads. See the Guide to Mood book for the
 * full explanation of what moves it and what it does.
 */
public final class Mood {
    public static final int MIN = 0;
    public static final int MAX = 100;
    public static final int DEFAULT = 50;

    public static final int LOW_THRESHOLD = 20;
    public static final int HIGH_THRESHOLD = 80;

    private static final String KEY = "junkcraft.mood";

    public static int get(Player player) {
        var data = player.getPersistentData();
        return data.contains(KEY) ? Mth.clamp(data.getInt(KEY), MIN, MAX) : DEFAULT;
    }

    public static void set(Player player, int value) {
        int clamped = Mth.clamp(value, MIN, MAX);
        player.getPersistentData().putInt(KEY, clamped);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MoodSyncPayload(clamped));
        }
    }

    public static void add(Player player, int delta) {
        set(player, get(player) + delta);
    }

    private Mood() {}
}
