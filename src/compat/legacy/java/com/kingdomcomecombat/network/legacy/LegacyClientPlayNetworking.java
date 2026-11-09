package com.kingdomcomecombat.network.legacy;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import java.util.function.BiConsumer;

/** Client half of the typed-payload compatibility bridge for Minecraft 1.20.1. */
public final class LegacyClientPlayNetworking {
    private LegacyClientPlayNetworking() {
    }

    public record Context(MinecraftClient client) {
    }

    public static <T extends LegacyCustomPayload> void registerGlobalReceiver(
            LegacyCustomPayload.Id<T> id,
            BiConsumer<T, Context> receiver
    ) {
        LegacyPacketCodec<PacketByteBuf, T> codec = LegacyPayloadTypeRegistry.playS2C().codec(id);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                id.id(),
                (client, handler, buffer, responseSender) -> {
                    T payload = codec.decode(buffer);
                    receiver.accept(payload, new Context(client));
                }
        );
    }

    public static <T extends LegacyCustomPayload> void send(T payload) {
        @SuppressWarnings("unchecked")
        LegacyCustomPayload.Id<T> id = (LegacyCustomPayload.Id<T>) payload.getId();
        LegacyPacketCodec<PacketByteBuf, T> codec = LegacyPayloadTypeRegistry.playC2S().codec(id);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        codec.encode(buffer, payload);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(id.id(), buffer);
    }

    public static boolean canSend(LegacyCustomPayload.Id<?> id) {
        return net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(id.id());
    }
}
