package io.papermc.paper.potion;

import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

record SuspiciousEffectEntryImpl(@NotNull PotionEffectType effect, int duration) implements SuspiciousEffectEntry {
}
