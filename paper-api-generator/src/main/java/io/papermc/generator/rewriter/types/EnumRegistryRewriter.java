package io.papermc.generator.rewriter.types;

import com.google.common.base.Suppliers;
import io.papermc.generator.Main;
import io.papermc.generator.rewriter.replace.SearchMetadata;
import io.papermc.generator.rewriter.utils.Annotations;
import io.papermc.generator.rewriter.ClassNamed;
import io.papermc.generator.utils.Formatting;
import io.papermc.generator.utils.RegistryUtils;
import io.papermc.generator.utils.experimental.ExperimentalHelper.FlagSets;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.Set;
import java.util.function.Supplier;

import static io.papermc.generator.utils.Formatting.quoted;

public class EnumRegistryRewriter<T, A extends Enum<A>> extends EnumRewriter<Holder.Reference<T>, A> {

    private final net.minecraft.core.Registry<T> registry;
    private final Supplier<Set<ResourceKey<T>>> experimentalKeys;
    private final boolean isFilteredRegistry;
    private final boolean hasParams;

    public EnumRegistryRewriter(final Class<A> rewriteClass, final ResourceKey<? extends Registry<T>> registryKey, final String pattern, final boolean hasParams) {
        this(new ClassNamed(rewriteClass), registryKey, pattern, hasParams);
    }

    public EnumRegistryRewriter(final ClassNamed rewriteClass, final ResourceKey<? extends Registry<T>> registryKey, final String pattern, final boolean hasParams) {
        super(rewriteClass, pattern, false);
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
    protected String rewriteEnumValue(Holder.Reference<T> reference) {
        if (this.hasParams) {
            return quoted(reference.key().location().getPath());
        }
        return null;
    }

    @Override
    protected void rewriteAnnotation(Holder.Reference<T> reference, StringBuilder builder, SearchMetadata metadata) {
        FeatureFlagSet requiredFeatures = this.getRequiredFeatures(reference);
        if (requiredFeatures != null) {
            Annotations.experimentalAnnotations(builder, metadata, requiredFeatures);
        }
    }

    @Nullable
    protected FeatureFlagSet getRequiredFeatures(Holder.Reference<T> reference) {
        if (this.isFilteredRegistry && reference.value() instanceof FeatureElement element && FeatureFlags.isExperimental(element.requiredFeatures())) {
            return element.requiredFeatures();
        }
        if (this.experimentalKeys.get().contains(reference.key())) {
            return FlagSets.NEXT_UPDATE.get();
        }
        return null;
    }
}
