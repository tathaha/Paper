package io.papermc.generator.rewriter.types;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import io.papermc.generator.Main;
import io.papermc.generator.rewriter.ClassNamed;
import io.papermc.generator.rewriter.replace.SearchMetadata;
import io.papermc.generator.rewriter.replace.SearchReplaceRewriter;
import io.papermc.generator.rewriter.utils.Annotations;
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
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

import static io.papermc.generator.utils.Formatting.quoted;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class RegistryFieldRewriter<T> extends SearchReplaceRewriter {

    private final Registry<T> registry;
    private final Supplier<Set<ResourceKey<T>>> experimentalKeys;
    private final boolean isFilteredRegistry;
    private final boolean isInterface;
    private final String fetchMethod;

    public RegistryFieldRewriter(final ClassNamed rewriteClass, final ResourceKey<? extends Registry<T>> registryKey, final String pattern, final boolean isInterface, final String fetchMethod) {
        super(rewriteClass, pattern, false);
        this.registry = Main.REGISTRY_ACCESS.registryOrThrow(registryKey);
        this.experimentalKeys = Suppliers.memoize(() -> RegistryUtils.collectExperimentalDataDrivenKeys(this.registry));
        this.isFilteredRegistry = FeatureElement.FILTERED_REGISTRIES.contains(registryKey);
        this.isInterface = isInterface;
        this.fetchMethod = fetchMethod;
    }

    public RegistryFieldRewriter(final Class<?> rewriteClass, final ResourceKey<? extends Registry<T>> registryKey, final String pattern, final @Nullable String fetchMethod) {
        this(new ClassNamed(rewriteClass), registryKey, pattern, rewriteClass.isInterface() && !rewriteClass.isAnnotation(), fetchMethod);
    }

    @Override
    protected void beginSearch() {
        Preconditions.checkState(this.rewriteClass.knownClass() != null, "This rewriter can't check the integrity of the fetch method since it doesn't know the rewritten class!");
        try {
            this.rewriteClass.knownClass().getDeclaredMethod(this.fetchMethod, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void insert(final SearchMetadata metadata, final StringBuilder builder) {
        Iterator<Holder.Reference<T>> referenceIterator = this.registry.holders().sorted(Formatting.alphabeticKeyOrder(reference -> reference.key().location().getPath())).iterator();

        while (referenceIterator.hasNext()) {
            Holder.Reference<T> reference = referenceIterator.next();
            ResourceKey<T> resourceKey = reference.key();
            String pathKey = resourceKey.location().getPath();

            FeatureFlagSet requiredFeatures = this.getRequiredFeatures(reference);
            if (requiredFeatures != null) {
                Annotations.experimentalAnnotations(builder, metadata, requiredFeatures);
            }

            builder.append(metadata.indent());
            if (!this.isInterface) {
                builder.append("%s %s %s ".formatted(PUBLIC, STATIC, FINAL));
            }

            builder.append(this.rewriteFieldType(reference)).append(' ').append(this.rewriteFieldName(reference));
            builder.append(" = ");
            builder.append("%s(%s)".formatted(this.fetchMethod, quoted(pathKey)));
            builder.append(';');

            builder.append('\n');
            if (referenceIterator.hasNext()) {
                builder.append('\n');
            }
        }
    }

    protected String rewriteFieldType(Holder.Reference<T> reference) {
        return this.rewriteClass.simpleName();
    }

    protected String rewriteFieldName(Holder.Reference<T> reference) {
        return Formatting.formatKeyAsField(reference.key().location().getPath());
    }

    protected @Nullable FeatureFlagSet getRequiredFeatures(Holder.Reference<T> reference) {
        if (this.isFilteredRegistry && reference.value() instanceof FeatureElement element && FeatureFlags.isExperimental(element.requiredFeatures())) {
            return element.requiredFeatures();
        }
        if (this.experimentalKeys.get().contains(reference.key())) {
            return FlagSets.NEXT_UPDATE.get();
        }
        return null;
    }
}
