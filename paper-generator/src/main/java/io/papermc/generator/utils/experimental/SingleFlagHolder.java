package io.papermc.generator.utils.experimental;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.bukkit.MinecraftExperimental;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

@DefaultQualifier(NonNull.class)
public record SingleFlagHolder(FeatureFlag flag) implements FlagHolder { // todo support multiple flags?

    static final FeatureFlag NEXT_UPDATE = FeatureFlags.UPDATE_1_21;

    private static final Map<String, FeatureFlag> FEATURE_FLAG_CACHE = new HashMap<>();
    private static final Map<FeatureFlag, ResourceLocation> FEATURE_FLAG_NAME = HashBiMap.create(FeatureFlags.REGISTRY.names).inverse();

    private static final Map<FeatureFlag, MinecraftExperimental.Requires> ANNOTATION_EQUIVALENT = Util.make(new IdentityHashMap<>(), map -> {
        map.put(NEXT_UPDATE, MinecraftExperimental.Requires.UPDATE_1_21);
        map.put(FeatureFlags.BUNDLE, MinecraftExperimental.Requires.BUNDLE);
        map.put(FeatureFlags.TRADE_REBALANCE, MinecraftExperimental.Requires.TRADE_REBALANCE);
    });

    public static SingleFlagHolder fromValue(FeatureFlag flag) {
        return new SingleFlagHolder(flag);
    }

    public static SingleFlagHolder fromSet(FeatureFlagSet standaloneSet) {
        Preconditions.checkArgument(Long.bitCount(standaloneSet.mask) == 1, "Flag set size must be equals to 1.");

        for (FeatureFlag flag : FeatureFlags.REGISTRY.names.values()) {
            if (standaloneSet.contains(flag)) {
                return fromValue(flag);
            }
        }

        throw new IllegalStateException();
    }

    public static SingleFlagHolder fromVanillaName(String name) {
        return fromValue(FEATURE_FLAG_CACHE.computeIfAbsent(name, key -> {
            return FeatureFlags.REGISTRY.names.get(new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, key));
        }));
    }

    public MinecraftExperimental.Requires asAnnotationMember() {
        MinecraftExperimental.Requires annotationMember = ANNOTATION_EQUIVALENT.get(this.flag);
        if (annotationMember == null) {
            throw new UnsupportedOperationException("Don't know that feature flag : " + FEATURE_FLAG_NAME.get(this.flag));
        }
        return annotationMember;
    }
}
