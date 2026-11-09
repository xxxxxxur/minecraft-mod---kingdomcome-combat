package com.kingdomcomecombat.compat;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;

import java.util.function.BiConsumer;

/** Item attribute-modifier view for the 1.20.1 stack API. */
public final class LegacyAttributeModifiers {
    private final ItemStack stack;

    private LegacyAttributeModifiers(ItemStack stack) {
        this.stack = stack;
    }

    public static LegacyAttributeModifiers from(ItemStack stack) {
        return new LegacyAttributeModifiers(stack);
    }

    public void applyModifiers(
            EquipmentSlot slot,
            BiConsumer<EntityAttribute, EntityAttributeModifier> consumer
    ) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        stack.getAttributeModifiers(slot).forEach(consumer);
    }
}
