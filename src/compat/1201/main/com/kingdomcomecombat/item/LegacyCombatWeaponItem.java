package com.kingdomcomecombat.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;

import java.util.UUID;

/** 1.20.1 item subclass used where later versions store attributes in components. */
final class LegacyCombatWeaponItem extends Item {
    private final int enchantability;
    private final Multimap<EntityAttribute, EntityAttributeModifier> modifiers;

    LegacyCombatWeaponItem(Settings settings, String name, double damage, double speed, int enchantability) {
        super(settings);
        this.enchantability = enchantability;
        this.modifiers = ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder()
                .put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(
                        modifierId(name + "_attack_damage"), "Weapon modifier", damage,
                        EntityAttributeModifier.Operation.ADDITION))
                .put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(
                        modifierId(name + "_attack_speed"), "Weapon modifier", speed,
                        EntityAttributeModifier.Operation.ADDITION))
                .build();
    }

    @Override
    public int getEnchantability() {
        return enchantability;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? modifiers : super.getAttributeModifiers(slot);
    }

    private static UUID modifierId(String value) {
        return UUID.nameUUIDFromBytes(("kingdom_come_combat:" + value)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
