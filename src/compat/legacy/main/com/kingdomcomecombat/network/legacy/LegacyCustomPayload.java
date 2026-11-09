package com.kingdomcomecombat.network.legacy;

import net.minecraft.util.Identifier;

/** Minimal 1.20.1 equivalent of the typed custom-payload contract added later. */
public interface LegacyCustomPayload {
    Id<? extends LegacyCustomPayload> getId();

    record Id<T extends LegacyCustomPayload>(Identifier id) {
    }
}
