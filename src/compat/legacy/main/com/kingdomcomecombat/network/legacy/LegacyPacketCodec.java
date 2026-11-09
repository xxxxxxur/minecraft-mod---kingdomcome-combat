package com.kingdomcomecombat.network.legacy;

import net.minecraft.network.PacketByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

/** Typed codec facade used to share the modern packet records with Fabric API 0.92. */
public final class LegacyPacketCodec<B extends PacketByteBuf, T> {
    private final BiConsumer<T, B> encoder;
    private final Function<B, T> decoder;

    private LegacyPacketCodec(BiConsumer<T, B> encoder, Function<B, T> decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public static <B extends PacketByteBuf, T> LegacyPacketCodec<B, T> of(
            BiConsumer<T, B> encoder,
            Function<B, T> decoder
    ) {
        return new LegacyPacketCodec<>(encoder, decoder);
    }

    public void encode(B buffer, T value) {
        encoder.accept(value, buffer);
    }

    public T decode(B buffer) {
        return decoder.apply(buffer);
    }
}
