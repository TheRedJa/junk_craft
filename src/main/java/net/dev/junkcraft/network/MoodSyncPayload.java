package net.dev.junkcraft.network;

import net.dev.junkcraft.JunkCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Sent server -> client whenever a player's Mood value changes, so the HUD can display it. */
public record MoodSyncPayload(int mood) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MoodSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(JunkCraft.MODID, "mood_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoodSyncPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, MoodSyncPayload::mood, MoodSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
