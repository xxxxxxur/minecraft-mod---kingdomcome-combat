package com.kingdomcomecombat.compat;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Read-only view matching the small part of the component API used by KCC. */
public final class LegacyItemEnchantments {
    private final List<Entry> entries;

    private LegacyItemEnchantments(List<Entry> entries) {
        this.entries = entries;
    }

    public static LegacyItemEnchantments from(ItemStack stack) {
        List<Entry> entries = new ArrayList<>();
        if (stack != null && !stack.isEmpty()) {
            EnchantmentHelper.get(stack).forEach((enchantment, level) ->
                    entries.add(new Entry(enchantment, level)));
        }
        return new LegacyItemEnchantments(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<Entry> getEnchantmentEntries() {
        return entries;
    }

    public record Entry(Enchantment enchantment, int level) {
        public Key getKey() {
            return new Key(enchantment);
        }

        public int getIntValue() {
            return level;
        }
    }

    public record Key(Enchantment enchantment) {
        public boolean matchesId(Identifier id) {
            return id != null && id.equals(Registries.ENCHANTMENT.getId(enchantment));
        }
    }
}
