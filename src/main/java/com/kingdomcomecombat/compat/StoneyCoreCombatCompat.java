package com.kingdomcomecombat.compat;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Optional bridge to StoneyCore's datapack-backed combat definitions. */
public final class StoneyCoreCombatCompat {
    private static final String KNIGHTS_HERALDRY_NAMESPACE = "knightsheraldry";
    private static final String WEAPON_UTIL_CLASS =
            "banduty.stoneycore.util.weaponutil.SCWeaponUtil";
    private static final String DAMAGE_TYPE_CLASS =
            "banduty.stoneycore.combat.damagetype.SCDamageType";
    private static final String UNDER_ARMOR_CLASS =
            "banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor";
    private static final String ATTACHMENT_SLOT_STORAGE_CLASS =
            "banduty.stoneycore.util.definitionsloader.ArmorAttachmentSlotDefinitionsStorage";
    private static final String ATTACHMENT_SLOT_DATA_CLASS =
            "banduty.stoneycore.util.definitionsloader.ArmorAttachmentSlotDefinitionData";

    private static boolean initialized;
    private static Method getMaxDamage;
    private static Object[] damageTypes = new Object[0];
    private static boolean armorInitialized;
    private static Method getArmorAttachments;
    private static Method getAttachmentSlotDefinitions;
    private static Method attachmentSlot;
    private static Method attachmentItems;

    private StoneyCoreCombatCompat() {
    }

    /**
     * Returns the strongest damage value supplied by the mod's loaded weapon
     * definitions. Zero means StoneyCore is absent or the item has no entry.
     */
    public static double weaponBaseDamage(ItemStack stack) {
        if (stack == null || stack.isEmpty()
                || !KNIGHTS_HERALDRY_NAMESPACE.equals(
                        Registries.ITEM.getId(stack.getItem()).getNamespace())) {
            return 0.0;
        }

        initialize();
        if (getMaxDamage == null) {
            return 0.0;
        }

        try {
            double strongest = 0.0;
            for (Object damageType : damageTypes) {
                Object value = getMaxDamage.invoke(null, damageType, stack.getItem());
                if (value instanceof Number number) {
                    strongest = Math.max(strongest, number.doubleValue());
                }
            }
            return strongest;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return 0.0;
        }
    }

    /**
     * Resolves body-part coverage from the attachment slots in StoneyCore's
     * currently loaded datapack. No K&H item names are hard-coded here.
     */
    public static Set<String> armorAttachmentProtectedParts(ItemStack stack) {
        if (stack == null || stack.isEmpty()
                || !KNIGHTS_HERALDRY_NAMESPACE.equals(
                        Registries.ITEM.getId(stack.getItem()).getNamespace())) {
            return Set.of();
        }

        initializeArmor();
        if (getArmorAttachments == null || getAttachmentSlotDefinitions == null) {
            return Set.of();
        }

        try {
            Object attachmentsValue = getArmorAttachments.invoke(null, stack);
            Object definitionsValue = getAttachmentSlotDefinitions.invoke(null);
            if (!(attachmentsValue instanceof Iterable<?> attachments)
                    || !(definitionsValue instanceof Map<?, ?> definitions)) {
                return Set.of();
            }

            Set<String> protectedParts = new HashSet<>();
            for (Object attachmentValue : attachments) {
                if (!(attachmentValue instanceof ItemStack attachment) || attachment.isEmpty()) {
                    continue;
                }
                String attachmentId = Registries.ITEM.getId(attachment.getItem()).toString();
                for (Object definition : definitions.values()) {
                    Object itemsValue = attachmentItems.invoke(definition);
                    if (!(itemsValue instanceof Iterable<?> items)
                            || !containsIdentifier(items, attachmentId)) {
                        continue;
                    }

                    String slot = String.valueOf(attachmentSlot.invoke(definition));
                    switch (slot) {
                        case "arm" -> protectedParts.add("arm");
                        case "gloves" -> protectedParts.add("hand");
                        case "spaulders" -> protectedParts.add("shoulder");
                        default -> {
                        }
                    }
                    break;
                }
            }
            return Set.copyOf(protectedParts);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return Set.of();
        }
    }

    private static boolean containsIdentifier(Iterable<?> identifiers, String expected) {
        for (Object identifier : identifiers) {
            if (expected.equals(String.valueOf(identifier))) {
                return true;
            }
        }
        return false;
    }

    private static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        try {
            ClassLoader loader = StoneyCoreCombatCompat.class.getClassLoader();
            Class<?> damageTypeClass = Class.forName(DAMAGE_TYPE_CLASS, false, loader);
            Class<?> weaponUtilClass = Class.forName(WEAPON_UTIL_CLASS, false, loader);
            Object[] constants = damageTypeClass.getEnumConstants();
            if (constants == null || constants.length == 0) {
                return;
            }

            for (Method method : weaponUtilClass.getMethods()) {
                if (method.getName().equals("getMaxDamage")
                        && method.getParameterCount() == 2
                        && method.getParameterTypes()[0] == damageTypeClass) {
                    getMaxDamage = method;
                    damageTypes = constants;
                    return;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            // Optional mod: vanilla and other modded weapons keep their normal path.
        }
    }

    private static synchronized void initializeArmor() {
        if (armorInitialized) {
            return;
        }
        armorInitialized = true;

        try {
            ClassLoader loader = StoneyCoreCombatCompat.class.getClassLoader();
            Class<?> underArmorClass = Class.forName(UNDER_ARMOR_CLASS, false, loader);
            Class<?> slotStorageClass = Class.forName(ATTACHMENT_SLOT_STORAGE_CLASS, false, loader);
            Class<?> slotDataClass = Class.forName(ATTACHMENT_SLOT_DATA_CLASS, false, loader);
            getArmorAttachments = underArmorClass.getMethod("getArmorAttachments", ItemStack.class);
            getAttachmentSlotDefinitions = slotStorageClass.getMethod("getDefinitions");
            attachmentSlot = slotDataClass.getMethod("slot");
            attachmentItems = slotDataClass.getMethod("items");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            getArmorAttachments = null;
            getAttachmentSlotDefinitions = null;
            attachmentSlot = null;
            attachmentItems = null;
        }
    }
}
