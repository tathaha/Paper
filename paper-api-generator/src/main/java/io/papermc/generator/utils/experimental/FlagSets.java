package io.papermc.generator.utils.experimental;

import com.google.common.base.Suppliers;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

import java.util.function.Supplier;

public class FlagSets {

    public static final Supplier<FeatureFlagSet> NEXT_UPDATE = standaloneSet(FeatureFlags.UPDATE_1_21);
    public static final Supplier<FeatureFlagSet> BUNDLE = standaloneSet(FeatureFlags.BUNDLE);

    private static Supplier<FeatureFlagSet> standaloneSet(FeatureFlag flag) {
        return Suppliers.memoize(() -> FeatureFlagSet.of(flag));
    }
}
