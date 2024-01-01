package io.papermc.paper.registry;

import com.google.common.collect.Sets;
import java.util.Set;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

record RegistryKeyImpl<T>(@NotNull Key key) implements RegistryKey<T> {

    static final Set<RegistryKey<?>> REGISTRY_KEYS = Sets.newIdentityHashSet();

    static <T> RegistryKey<T> create(@Subst("some_key") final String key) {
        final RegistryKey<T> registryKey = new RegistryKeyImpl<>(Key.key(Key.MINECRAFT_NAMESPACE, key));
        REGISTRY_KEYS.add(registryKey);
        return registryKey;
    }

}
