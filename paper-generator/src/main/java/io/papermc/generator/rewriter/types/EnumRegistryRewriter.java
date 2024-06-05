package io.papermc.generator.rewriter.types;

import com.google.common.base.Suppliers;
import io.papermc.generator.Main;
import io.papermc.generator.rewriter.utils.Annotations;
import io.papermc.generator.utils.Formatting;
import io.papermc.generator.utils.RegistryUtils;
import io.papermc.generator.utils.experimental.FlagHolders;
import io.papermc.generator.utils.experimental.SingleFlagHolder;
import io.papermc.typewriter.preset.EnumRewriter;
import io.papermc.typewriter.replace.SearchMetadata;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlags;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.Set;
import java.util.function.Supplier;

import static io.papermc.typewriter.utils.Formatting.quoted;

public class EnumRegistryRewriter<T> extends EnumRewriter<Holder.Reference<T>> {

    private final net.minecraft.core.Registry<T> registry;
    private final Supplier<Set<ResourceKey<T>>> experimentalKeys;
    private final boolean isFilteredRegistry;
    private final boolean hasParams;

    public EnumRegistryRewriter(final ResourceKey<? extends Registry<T>> registryKey, final boolean hasParams) {
        this.registry = Main.REGISTRY_ACCESS.registryOrThrow(registryKey);
        this.experimentalKeys = Suppliers.memoize(() -> RegistryUtils.collectExperimentalDataDrivenKeys(this.registry));
        this.isFilteredRegistry = FeatureElement.FILTERED_REGISTRIES.contains(registryKey);
        this.hasParams = hasParams;
    }

    @Override
    protected Iterable<Holder.Reference<T>> getValues() {
        return this.registry.holders().sorted(Formatting.alphabeticKeyOrder(reference -> reference.key().location().getPath())).toList();
    }

    @Override
    protected String rewriteEnumName(Holder.Reference<T> reference) {
        return Formatting.formatKeyAsField(reference.key().location().getPath());
    }

    @Override
    protected @Nullable String rewriteEnumValue(Holder.Reference<T> reference) {
        if (this.hasParams) {
            return quoted(reference.key().location().getPath());
        }
        return null;
    }

    @Override
    protected void rewriteAnnotation(Holder.Reference<T> reference, StringBuilder builder, SearchMetadata metadata) {
        @Nullable SingleFlagHolder requiredFeature = this.getRequiredFeature(reference);
        if (requiredFeature != null) {
            Annotations.experimentalAnnotations(builder, metadata, requiredFeature);
        }
    }

    protected @Nullable SingleFlagHolder getRequiredFeature(Holder.Reference<T> reference) {
        if (this.isFilteredRegistry) {
            // built-in registry
            FeatureElement element = (FeatureElement) reference.value();
            if (FeatureFlags.isExperimental(element.requiredFeatures())) {
                return SingleFlagHolder.fromSet(element.requiredFeatures());
            }
        } else {
            // data-driven registry
            if (this.experimentalKeys.get().contains(reference.key())) {
                return FlagHolders.NEXT_UPDATE;
            }
        }
        return null;
    }
}
