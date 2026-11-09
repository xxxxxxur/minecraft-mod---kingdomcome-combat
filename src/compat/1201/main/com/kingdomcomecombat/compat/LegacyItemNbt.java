package com.kingdomcomecombat.compat;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.function.Consumer;

/** Item data bridge for Minecraft 1.20.1, before vanilla data components existed. */
public final class LegacyItemNbt {
    private LegacyItemNbt() {
    }

    public static NbtCompound get(ItemStack stack) {
        return stack == null ? null : stack.getNbt();
    }

    public static void edit(ItemStack stack, Consumer<NbtCompound> editor) {
        if (stack != null && editor != null) {
            editor.accept(stack.getOrCreateNbt());
        }
    }
}
