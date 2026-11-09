package com.kingdomcomecombat.network;

import com.kingdomcomecombat.KingdomComeCombat;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Enables or quarantines automatic humanoid compatibility for an observed model type. */
public record BipedModelDetectedPayload(int entityId, boolean compatible) implements CustomPayload {
    public static final Id<BipedModelDetectedPayload> ID =
            new Id<>(Identifier.of(KingdomComeCombat.MOD_ID, "biped_model_detected"));
    public static final PacketCodec<RegistryByteBuf, BipedModelDetectedPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeVarInt(value.entityId());
                buf.writeBoolean(value.compatible());
            },
            buf -> new BipedModelDetectedPayload(buf.readVarInt(), buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
