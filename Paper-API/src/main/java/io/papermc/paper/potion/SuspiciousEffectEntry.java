package io.papermc.paper.potion;

import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link PotionEffectType} paired with a duration.
 */
public sealed interface SuspiciousEffectEntry permits SuspiciousEffectEntryImpl {

    /**
      * Gets the effect type.
     *
      * @return type
      */
    @NotNull PotionEffectType effect();

    /**
      * Gets the duration for this effect instance.
      *
      * @return duration (in ticks)
      */
    int duration();

    /**
     * Creates a new instance of SuspiciousEffectEntry.
     *
     * @param effectType effect type
     * @param duration duration (in ticks)
     * @return new instance of an entry
     */
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull SuspiciousEffectEntry create(final @NotNull PotionEffectType effectType, final int duration) {
        return new SuspiciousEffectEntryImpl(effectType, duration);
    }
}
