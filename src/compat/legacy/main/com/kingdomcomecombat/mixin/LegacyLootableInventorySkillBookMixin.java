package com.kingdomcomecombat.mixin;

import com.kingdomcomecombat.item.SkillBookAcquisition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric's post-generation loot event did not exist on the legacy adapter.
 * Add the fully populated, data-pack-backed book after a lootable inventory
 * generates instead of inserting an unconfigured skill_book item in the table.
 */
@Mixin(LootableInventory.class)
public abstract class LegacyLootableInventorySkillBookMixin {
    @Shadow public abstract RegistryKey<LootTable> getLootTable();
    @Shadow public abstract int size();
    @Shadow public abstract ItemStack getStack(int slot);
    @Shadow public abstract void setStack(int slot, ItemStack stack);

    @Unique private RegistryKey<LootTable> kingdomcomecombat$generatedLootTable;

    @Inject(method = "generateLoot", at = @At("HEAD"))
    private void kingdomcomecombat$captureLootTable(PlayerEntity player, CallbackInfo ci) {
        kingdomcomecombat$generatedLootTable = getLootTable();
    }

    @Inject(method = "generateLoot", at = @At("RETURN"))
    private void kingdomcomecombat$addGeneratedSkillBook(PlayerEntity player, CallbackInfo ci) {
        RegistryKey<LootTable> key = kingdomcomecombat$generatedLootTable;
        kingdomcomecombat$generatedLootTable = null;
        if (!(player instanceof ServerPlayerEntity serverPlayer) || key == null) {
            return;
        }

        Identifier id = key.getValue();
        String path = id.getPath();
        if (!"minecraft".equals(id.getNamespace())
                || !SkillBookAcquisition.isSkillBookChest(path)
                || serverPlayer.getRandom().nextFloat() > SkillBookAcquisition.lootChance(path)) {
            return;
        }

        ItemStack book = SkillBookAcquisition.randomSkillBook(serverPlayer.getRandom(), serverPlayer);
        if (book.isEmpty()) {
            return;
        }

        int start = serverPlayer.getRandom().nextInt(Math.max(1, size()));
        for (int offset = 0; offset < size(); offset++) {
            int slot = (start + offset) % size();
            if (getStack(slot).isEmpty()) {
                setStack(slot, book);
                return;
            }
        }
    }
}
