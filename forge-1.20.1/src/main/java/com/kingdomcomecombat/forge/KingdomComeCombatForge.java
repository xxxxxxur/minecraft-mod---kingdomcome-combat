package com.kingdomcomecombat.forge;

import com.kingdomcomecombat.KingdomComeCombat;
import com.kingdomcomecombat.client.KingdomComeCombatClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

/** Forge loader boundary; gameplay and data stay in the shared 1.20.1 code. */
@Mod(KingdomComeCombat.MOD_ID)
public final class KingdomComeCombatForge {
    public KingdomComeCombatForge() {
        new KingdomComeCombat().onInitialize();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> new KingdomComeCombatClient().onInitializeClient());
    }
}
