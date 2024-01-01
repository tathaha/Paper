package io.papermc.paper.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

record TypedKeyImpl<T extends Keyed>(@NotNull Key key, @NotNull RegistryKey<T> registryKey) implements TypedKey<T> {
}
