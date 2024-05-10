package io.papermc.generator.rewriter.types.simple;

import io.papermc.generator.rewriter.types.RegistryFieldRewriter;
import io.papermc.generator.utils.ClassHelper;
import io.papermc.generator.utils.experimental.ExperimentalHelper.FlagSets;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.Nullable;

@Deprecated // bad generic
public class ItemTypeRewriter extends RegistryFieldRewriter<Item> {

    public ItemTypeRewriter(final String pattern) {
        super(ItemType.class, Registries.ITEM, pattern, "getItemType");
    }

    @Override
    protected String rewriteFieldType(Holder.Reference<Item> reference) {
        if (reference.value().equals(Items.AIR)) {
            return super.rewriteFieldType(reference);
        }

        return "%s<%s>".formatted(ClassHelper.retrieveFullNestedName(ItemType.Typed.class), ItemMeta.class.getSimpleName());
    }

    @Override
    protected @Nullable FeatureFlagSet getRequiredFeatures(Holder.Reference<Item> reference) {
        if (reference.value() instanceof BundleItem) {
            return FlagSets.BUNDLE.get(); // special case since the item is not locked itself just in the creative menu
        } else {
            return super.getRequiredFeatures(reference);
        }
    }
}
