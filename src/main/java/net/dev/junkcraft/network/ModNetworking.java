package net.dev.junkcraft.network;

import net.dev.junkcraft.mood.ClientMoodCache;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(MoodSyncPayload.TYPE, MoodSyncPayload.STREAM_CODEC,
                (payload, context) -> ClientMoodCache.set(payload.mood()));
    }
}
