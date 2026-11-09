package com.zigythebird.playeranim;

import com.zigythebird.playeranim.fabric.client.IdentifiablePlayerAnimResources;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

/** Initializes the source-backported PAL runtime bundled with the 1.20.1 build. */
public final class Legacy1201PalBootstrap extends PlayerAnimLibMod {
    private static boolean initialized;

    private Legacy1201PalBootstrap() {
    }

    public static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new IdentifiablePlayerAnimResources());
        new Legacy1201PalBootstrap().init();
    }
}
