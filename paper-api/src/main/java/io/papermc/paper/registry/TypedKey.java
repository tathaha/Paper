package io.papermc.paper.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a key for a value in a specific registry.
 *
 * @param <T> the value type for the registry
 */
@ApiStatus.Experimental
public sealed interface TypedKey<T> extends Keyed permits TypedKeyImpl {

    /**
     * Gets the key for the value in the registry.
     *
     * @return the value's key
     */
    @Override
    @NotNull Key key();

    /**
     * Gets the registry key for the value this key
     * represents.
     *
     * @return the registry key
     */
    @NotNull RegistryKey<T> registryKey();

    /**
     * Create a typed key from a key and a registry key.
     *
     * @param registryKey the registry this key is for
     * @param key the key for the value in the registry
     * @param <T> value type
     * @return a new key for the value key and registry key
     */
    @ApiStatus.Experimental
    static <T extends Keyed> @NotNull TypedKey<T> create(final @NotNull RegistryKey<T> registryKey, final @NotNull Key key) {
        return new TypedKeyImpl<>(key, registryKey);
    }
}
