package com.kingdomcomecombat.compat;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** Potion-component equivalent backed by 1.20.1's item NBT. */
public final class LegacyPotionContents {
    public static final String DRAGON_BREATH_NAME = "kingdom_come_combat.dragon_breath_arrow";
    private final Potion potion;
    private final List<StatusEffectInstance> effects;
    private final int color;
    private final String customName;

    private LegacyPotionContents(Potion potion, List<StatusEffectInstance> effects, int color, String customName) {
        this.potion = potion;
        this.effects = List.copyOf(effects);
        this.color = color;
        this.customName = customName;
    }

    public LegacyPotionContents(Optional<?> ignoredPotion, Optional<Integer> color, List<?> effects) {
        this(null, effects.stream().filter(StatusEffectInstance.class::isInstance)
                .map(StatusEffectInstance.class::cast).toList(), color.orElse(0), "");
    }

    public static LegacyPotionContents from(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
        String name = stack.getOrCreateNbt().getString("kingdom_come_combat_potion_name");
        if (effects.isEmpty() && name.isEmpty()) return null;
        return new LegacyPotionContents(PotionUtil.getPotion(stack), effects, PotionUtil.getColor(stack), name);
    }

    public static LegacyPotionContents dragonBreath(int color) {
        return new LegacyPotionContents(null, List.of(), color, DRAGON_BREATH_NAME);
    }

    public boolean hasEffects() {
        return !effects.isEmpty();
    }

    public Optional<String> customName() {
        return customName.isEmpty() ? Optional.empty() : Optional.of(customName);
    }

    public Optional<Integer> customColor() {
        return color == 0 ? Optional.empty() : Optional.of(color);
    }

    public void applyTo(ItemStack stack) {
        if (potion != null) PotionUtil.setPotion(stack, potion);
        PotionUtil.setCustomPotionEffects(stack, effects);
        if (color != 0) stack.getOrCreateNbt().putInt(PotionUtil.CUSTOM_POTION_COLOR_KEY, color);
        if (!customName.isEmpty()) stack.getOrCreateNbt().putString("kingdom_come_combat_potion_name", customName);
    }

    public void forEachEffect(Consumer<EffectView> consumer, float ignoredScale) {
        for (StatusEffectInstance effect : effects) consumer.accept(new EffectView(effect));
    }

    public void forEachEffect(Consumer<EffectView> consumer) {
        forEachEffect(consumer, 1.0F);
    }

    public record EffectView(StatusEffectInstance effect) {
        public net.minecraft.entity.effect.StatusEffect getEffectType() { return effect.getEffectType(); }
        public int getDuration() { return effect.getDuration(); }
        public int getAmplifier() { return effect.getAmplifier(); }
        public boolean isAmbient() { return effect.isAmbient(); }
        public boolean shouldShowParticles() { return effect.shouldShowParticles(); }
        public boolean shouldShowIcon() { return effect.shouldShowIcon(); }

        public StatusEffectInstance withScaledDuration(float scale) {
            return new StatusEffectInstance(effect.getEffectType(), Math.max(1, Math.round(effect.getDuration() * scale)),
                    effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon());
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LegacyPotionContents other)) return false;
        return potion == other.potion && color == other.color && customName.equals(other.customName)
                && effects.equals(other.effects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(potion, effects, color, customName);
    }
}
