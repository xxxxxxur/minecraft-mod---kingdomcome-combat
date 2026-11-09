package com.kingdomcomecombat.client.compat;

import com.kingdomcomecombat.ai.HumanoidCombatAiProfiles;
import com.kingdomcomecombat.network.BipedModelDetectedPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Automatically enables safe rendered bipeds and quarantines incompatible model types. */
public final class BipedModelAiCompatReporter {
    private static final Set<net.minecraft.entity.EntityType<?>> ENABLED_TYPES =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<net.minecraft.entity.EntityType<?>> DISABLED_TYPES =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private BipedModelAiCompatReporter() {
    }

    public static void observe(LivingEntity entity, boolean hasCompleteBipedRig) {
        if (!isAutomaticCandidate(entity) || DISABLED_TYPES.contains(entity.getType())) {
            return;
        }
        if (!hasCompleteBipedRig
                || !HumanoidCombatAiProfiles.hasPlausibleAutomaticHumanoidDimensions(entity)) {
            disable(entity);
            return;
        }
        if (!ENABLED_TYPES.add(entity.getType())) {
            return;
        }

        HumanoidCombatAiProfiles.includeAutomaticallyDetectedType(entity.getType());
        send(entity, true);
    }

    public static boolean disableAfterFailure(LivingEntity entity) {
        if (!isAutomaticCandidate(entity)) {
            return false;
        }
        disable(entity);
        return true;
    }

    public static boolean isRenderDisabled(LivingEntity entity) {
        return entity != null && DISABLED_TYPES.contains(entity.getType());
    }

    public static void clear() {
        ENABLED_TYPES.clear();
        DISABLED_TYPES.clear();
    }

    private static void disable(LivingEntity entity) {
        boolean newlyDisabled = DISABLED_TYPES.add(entity.getType());
        ENABLED_TYPES.remove(entity.getType());
        HumanoidCombatAiProfiles.excludeAutomaticType(entity.getType());
        if (newlyDisabled) {
            send(entity, false);
        }
    }

    private static boolean isAutomaticCandidate(LivingEntity entity) {
        return entity instanceof MobEntity
                && !"minecraft".equals(
                        Registries.ENTITY_TYPE.getId(entity.getType()).getNamespace());
    }

    private static void send(LivingEntity entity, boolean compatible) {
        try {
            if (ClientPlayNetworking.canSend(BipedModelDetectedPayload.ID)) {
                ClientPlayNetworking.send(
                        new BipedModelDetectedPayload(entity.getId(), compatible)
                );
            }
        } catch (RuntimeException | LinkageError ignored) {
            // Rendering must remain safe while a connection is changing state.
        }
    }
}
