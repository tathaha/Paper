package io.papermc.generator.utils;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.Sets;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.UpdateOneTwentyOneRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RegistryUtils {

    private static final Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryBootstrap<?>> VANILLA_REGISTRY_ENTRIES = VanillaRegistries.BUILDER.entries.stream()
        .collect(Collectors.toMap(RegistrySetBuilder.RegistryStub::key, RegistrySetBuilder.RegistryStub::bootstrap));

    private static final Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryBootstrap<?>> EXPERIMENTAL_REGISTRY_ENTRIES = UpdateOneTwentyOneRegistries.BUILDER.entries.stream()
        .collect(Collectors.toMap(RegistrySetBuilder.RegistryStub::key, RegistrySetBuilder.RegistryStub::bootstrap));

    @SuppressWarnings("unchecked")
    public static <T> Set<ResourceKey<T>> collectExperimentalDataDrivenKeys(final Registry<T> registry) {
        final RegistrySetBuilder.@Nullable RegistryBootstrap<T> experimentalBootstrap = (RegistrySetBuilder.RegistryBootstrap<T>) EXPERIMENTAL_REGISTRY_ENTRIES.get(registry.key());
        if (experimentalBootstrap == null) {
            return Collections.emptySet();
        }
        final Set<ResourceKey<T>> experimental = Collections.newSetFromMap(new IdentityHashMap<>());
        final CollectingContext<T> experimentalCollector = new CollectingContext<>(experimental, registry);
        experimentalBootstrap.run(experimentalCollector);

        final RegistrySetBuilder.@Nullable RegistryBootstrap<T> vanillaBootstrap = (RegistrySetBuilder.RegistryBootstrap<T>) VANILLA_REGISTRY_ENTRIES.get(registry.key());
        if (vanillaBootstrap != null) {
            final Set<ResourceKey<T>> vanilla = Collections.newSetFromMap(new IdentityHashMap<>());
            final CollectingContext<T> vanillaCollector = new CollectingContext<>(vanilla, registry);
            vanillaBootstrap.run(vanillaCollector);
            return Sets.difference(experimental, vanilla);
        }
        return experimental;
    }
}
