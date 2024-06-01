package io.papermc.generator.utils.experimental;

import net.minecraft.world.flag.FeatureFlags;

public class FlagHolders {

    public static final SingleFlagHolder NEXT_UPDATE = SingleFlagHolder.fromValue(SingleFlagHolder.NEXT_UPDATE);
    public static final SingleFlagHolder BUNDLE = SingleFlagHolder.fromValue(FeatureFlags.BUNDLE);
    public static final SingleFlagHolder TRADE_REBALANCE = SingleFlagHolder.fromValue(FeatureFlags.TRADE_REBALANCE);

}
