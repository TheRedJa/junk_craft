package net.dev.junkcraft.mood;

/**
 * Client-side cache of the local player's Mood value, kept in sync by
 * {@link net.dev.junkcraft.network.MoodSyncPayload}. Mood itself lives in
 * server-side player persistent data, which isn't visible on the client, so the
 * HUD renderer reads from here instead.
 */
public final class ClientMoodCache {
    private static volatile int mood = Mood.DEFAULT;

    public static int get() {
        return mood;
    }

    public static void set(int value) {
        mood = value;
    }

    private ClientMoodCache() {}
}
