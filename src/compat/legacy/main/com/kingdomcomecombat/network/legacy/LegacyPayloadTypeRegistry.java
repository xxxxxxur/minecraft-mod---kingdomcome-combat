package com.kingdomcomecombat.network.legacy;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/** Stores the codecs that 1.21 registers with PayloadTypeRegistry. */
public final class LegacyPayloadTypeRegistry {
    private static final Registry C2S = new Registry();
    private static final Registry S2C = new Registry();

    private LegacyPayloadTypeRegistry() {
    }

    public static Registry playC2S() { return C2S; }
    public static Registry playS2C() { return S2C; }

    public static final class Registry {
        private final Map<Identifier, LegacyPacketCodec<PacketByteBuf, ?>> codecs = new HashMap<>();

        @SuppressWarnings("unchecked")
        public <T extends LegacyCustomPayload> void register(
                LegacyCustomPayload.Id<T> id,
                LegacyPacketCodec<? extends PacketByteBuf, T> codec
        ) {
            codecs.put(id.id(), (LegacyPacketCodec<PacketByteBuf, ?>) codec);
        }

        @SuppressWarnings("unchecked")
        public <T extends LegacyCustomPayload> LegacyPacketCodec<PacketByteBuf, T> codec(
                LegacyCustomPayload.Id<T> id
        ) {
            return (LegacyPacketCodec<PacketByteBuf, T>) codecs.get(id.id());
        }
    }
}
