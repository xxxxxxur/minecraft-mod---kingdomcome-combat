package com.kingdomcomecombat.forge;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;

/** Forge does not expose the Fabric-access-widened DamageSources#create methods. */
public final class LegacyForgeDamageSources {
    private LegacyForgeDamageSources() {
    }

    public static DamageSource create(ServerWorld world, RegistryKey<DamageType> key) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key));
    }

    public static DamageSource create(ServerWorld world, RegistryKey<DamageType> key, Entity source) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), source);
    }

    public static DamageSource create(
            ServerWorld world,
            RegistryKey<DamageType> key,
            Entity source,
            Entity attacker
    ) {
        return new DamageSource(
                world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key),
                source,
                attacker
        );
    }
}
