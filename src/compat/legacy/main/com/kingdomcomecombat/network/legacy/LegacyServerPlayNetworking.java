package com.kingdomcomecombat.network.legacy;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiConsumer;

/** Adapts typed KCC packets to Fabric's identifier/PacketByteBuf 1.20.1 API. */
public final class LegacyServerPlayNetworking {
    private LegacyServerPlayNetworking() {
    }

    public record Context(ServerPlayerEntity player) {
        public net.minecraft.server.MinecraftServer server() {
            return player.getServer();
        }
    }

    public static <T extends LegacyCustomPayload> void registerGlobalReceiver(
            LegacyCustomPayload.Id<T> id,
            BiConsumer<T, Context> receiver
    ) {
        LegacyPacketCodec<PacketByteBuf, T> codec = LegacyPayloadTypeRegistry.playC2S().codec(id);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
                id.id(),
                (server, player, handler, buffer, responseSender) -> {
                    T payload = codec.decode(buffer);
                    server.execute(() -> receiver.accept(payload, new Context(player)));
                }
        );
    }

    public static <T extends LegacyCustomPayload> void send(ServerPlayerEntity player, T payload) {
        @SuppressWarnings("unchecked")
        LegacyCustomPayload.Id<T> id = (LegacyCustomPayload.Id<T>) payload.getId();
        LegacyPacketCodec<PacketByteBuf, T> codec = LegacyPayloadTypeRegistry.playS2C().codec(id);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        codec.encode(buffer, payload);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, id.id(), buffer);
    }

    public static boolean canSend(ServerPlayerEntity player, LegacyCustomPayload.Id<?> id) {
        return net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(player, id.id());
    }
}
