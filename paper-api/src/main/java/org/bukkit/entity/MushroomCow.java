package org.bukkit.entity;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a mushroom {@link Cow}
 */
public interface MushroomCow extends Cow, io.papermc.paper.entity.Shearable { // Paper

    /**
     * Get the variant of this cow.
     *
     * @return cow variant
     */
    @NotNull
    public Variant getVariant();

    /**
     * Set the variant of this cow.
     *
     * @param variant cow variant
     */
    public void setVariant(@NotNull Variant variant);

    /**
     * Represents the variant of a cow - ie its color.
     */
    public enum Variant {
        /**
         * Red mushroom cow.
         */
        RED,
        /**
         * Brown mushroom cow.
         */
        BROWN;
    }
    // Paper start
    /**
     * Gets how long the effect applied to stew
     * from this mushroom cow is.
     *
     * @return duration of the effect (in ticks)
     * @deprecated Mushroom cows can now hold multiple effects, use {@link #getStewEffects()}
     */
    @Deprecated(forRemoval = true)
    @org.jetbrains.annotations.Contract("-> fail")
    default int getStewEffectDuration() {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #getStewEffects");
    }

    /**
     * Sets how long the effect applied to stew
     * from this mushroom cow is.
     *
     * @param duration duration of the effect (in ticks)
     * @deprecated Mushroom cows can now hold multiple effects, use {@link #setStewEffects(java.util.List)}
     */
    @Deprecated(forRemoval = true)
    @org.jetbrains.annotations.Contract("_ -> fail")
    default void setStewEffectDuration(int duration) {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #setStewEffects");
    }

    /**
     * Gets the type of effect applied to stew
     * from this mushroom cow is.
     *
     * @return effect type, or null if an effect is currently not set
     * @deprecated Mushroom cows can now hold multiple effects, use {@link #getStewEffects()}
     * @throws UnsupportedOperationException
     */
    @Deprecated(forRemoval = true)
    @org.jetbrains.annotations.Contract("-> fail")
    default org.bukkit.potion.PotionEffectType getStewEffectType() {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #getStewEffects");
    }

    /**
     * Sets the type of effect applied to stew
     * from this mushroom cow is.
     *
     * @param type new effect type
     *             or null if this cow does not give effects
     * @deprecated Mushroom cows can now hold multiple effects, use {@link #setStewEffects(java.util.List)}
     * @throws UnsupportedOperationException
     */
    @Deprecated(forRemoval = true)
    @org.jetbrains.annotations.Contract("_ -> fail")
    default void setStewEffect(@org.jetbrains.annotations.Nullable org.bukkit.potion.PotionEffectType type) {
        throw new UnsupportedOperationException("Mushroom cows can now hold multiple effects. Use #setStewEffects");
    }

    /**
     * Returns an immutable collection of the effects applied to stew
     * items for this mushroom cow.
     *
     * @return immutable effect entry collection
     */
    java.util.@NotNull @org.jetbrains.annotations.Unmodifiable List<io.papermc.paper.potion.SuspiciousEffectEntry> getStewEffects();

    /**
     * Sets effects applied to stew items for this mushroom cow.
     *
     * @param effects effect entry list
     */
    void setStewEffects(java.util.@NotNull List<io.papermc.paper.potion.SuspiciousEffectEntry> effects);
    // Paper end
}
