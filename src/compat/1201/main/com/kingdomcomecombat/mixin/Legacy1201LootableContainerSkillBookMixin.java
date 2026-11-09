package com.kingdomcomecombat.mixin;

import com.kingdomcomecombat.item.SkillBookAcquisition;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds configured skill books after 1.20.1 generates a vanilla chest's loot. */
@Mixin(LootableContainerBlockEntity.class)
public abstract class Legacy1201LootableContainerSkillBookMixin {
    @Shadow protected Identifier lootTableId;
    @Shadow public abstract int size();
    @Shadow public abstract ItemStack getStack(int slot);
    @Shadow public abstract void setStack(int slot, ItemStack stack);

    @Unique private Identifier kingdomcomecombat$generatedLootTable;

    @Inject(method = "checkLootInteraction", at = @At("HEAD"))
    private void kingdomcomecombat$captureLootTable(PlayerEntity player, CallbackInfo ci) {
        kingdomcomecombat$generatedLootTable = lootTableId;
    }

    @Inject(method = "checkLootInteraction", at = @At("RETURN"))
    private void kingdomcomecombat$addGeneratedSkillBook(PlayerEntity player, CallbackInfo ci) {
        Identifier id = kingdomcomecombat$generatedLootTable;
        kingdomcomecombat$generatedLootTable = null;
        if (!(player instanceof ServerPlayerEntity serverPlayer) || id == null
                || !"minecraft".equals(id.getNamespace())) {
            return;
        }

        String path = id.getPath();
        if (!SkillBookAcquisition.isSkillBookChest(path)
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
