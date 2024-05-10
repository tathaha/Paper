package io.papermc.generator.rewriter.types.simple;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import io.papermc.generator.rewriter.types.EnumRegistryRewriter;
import io.papermc.generator.rewriter.types.SwitchRewriter;
import io.papermc.generator.utils.BlockStateMapping;
import io.papermc.generator.utils.Formatting;
import io.papermc.generator.utils.experimental.ExperimentalHelper.FlagSets;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallSignBlock;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.EquipmentSlot;
import org.checkerframework.checker.nullness.qual.Nullable;

@Deprecated(forRemoval = true)
public class MaterialRewriter {

    // blocks

    public static class Blocks extends EnumRegistryRewriter<Block, Material> {

        public Blocks(final String pattern) {
            super(Material.class, Registries.BLOCK, pattern, true);
        }

        @Override
        protected Iterable<Holder.Reference<Block>> getValues() {
            return BuiltInRegistries.BLOCK.holders().filter(reference -> !reference.value().equals(net.minecraft.world.level.block.Blocks.AIR))
                .sorted(Formatting.alphabeticKeyOrder(reference -> reference.key().location().getPath())).toList();
        }

        @Override
        protected String rewriteEnumValue(Holder.Reference<Block> reference) {
            Block block = reference.value();
            if (BlockStateMapping.MAPPING.containsKey(block.getClass())) {
                // some block can also be represented as item in that enum
                // doing a double job
                Optional<Item> equivalentItem = BuiltInRegistries.ITEM.getOptional(reference.key().location());

                if (equivalentItem.isEmpty() && block instanceof WallSignBlock) {
                    // wall sign block stack size is 16 for some reason like the sign item?
                    // but that rule doesn't work for the wall hanging sign block??
                    equivalentItem = Optional.of(block.asItem());
                }

                @Nullable Class<?> blockData = BlockStateMapping.getBestSuitedApiClass(block.getClass());
                if (blockData == null) {
                    blockData = BlockData.class;
                }
                if (equivalentItem.isPresent() && equivalentItem.get().getDefaultMaxStackSize() != Item.DEFAULT_MAX_STACK_SIZE) {
                    return "%d, %d, %s.class".formatted(-1, equivalentItem.get().getDefaultMaxStackSize(), blockData.getSimpleName());
                }
                return "%d, %s.class".formatted(-1, blockData.getSimpleName());
            }
            return String.valueOf(-1); // id not needed for non legacy material
        }
    }

    /* todo test is broken
    public static class IsTransparent extends SwitchCaseRewriter {

        public IsTransparent(final String pattern) {
            super(Material.class, pattern, false);
        }

        @Override
        protected Iterable<String> getCases() {
            return BuiltInRegistries.BLOCK.holders().filter(reference -> reference.value().defaultBlockState().useShapeForLightOcclusion())
            .map(reference -> reference.key().location().getPath().toUpperCase(Locale.ENGLISH)).sorted(Formatting.ALPHABETIC_KEY_ORDER).toList();
        }
    }*/

    // items

    public static class Items extends EnumRegistryRewriter<Item, Material> {

        public Items(final String pattern) {
            super(Material.class, Registries.ITEM, pattern, true);
        }

        @Override
        protected Iterable<Holder.Reference<Item>> getValues() {
            return BuiltInRegistries.ITEM.holders().filter(reference -> BuiltInRegistries.BLOCK.getOptional(reference.key().location()).isEmpty() || reference.value().equals(net.minecraft.world.item.Items.AIR))
                .sorted(Formatting.alphabeticKeyOrder(reference -> reference.key().location().getPath())).toList();
        }

        @Override
        protected String rewriteEnumValue(Holder.Reference<Item> reference) {
            Item item = reference.value();
            if (item.equals(net.minecraft.world.item.Items.AIR)) {
                return "%d, %d".formatted(-1, 0); // item+block
            }

            int maxStackSize = item.getDefaultMaxStackSize();
            int maxDamage = item.components().getOrDefault(DataComponents.MAX_DAMAGE, 0);

            if (maxStackSize != Item.DEFAULT_MAX_STACK_SIZE) {
                if (maxDamage != 0) {
                    return "%d, %d, %d".formatted(-1, maxStackSize, maxDamage);
                }
                return "%d, %d".formatted(-1, maxStackSize);
            }

            return String.valueOf(-1); // id not needed for non legacy material
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

    public static class GetEquipmentSlot extends SwitchRewriter<EquipmentSlot> {

        public GetEquipmentSlot(final String pattern) {
            super(Material.class, pattern, false);
            this.defaultValue = returnOf(EquipmentSlot.HAND, "%s.%s".formatted(EquipmentSlot.class.getSimpleName(), EquipmentSlot.HAND.name()));
        }

        @Override
        protected Multimap<Return<EquipmentSlot>, String> getContent() {
            Multimap<Return<EquipmentSlot>, String> map = MultimapBuilder.treeKeys(Comparator.<Return<EquipmentSlot>>comparingInt(key -> key.object().ordinal()).reversed())
                .treeSetValues(Formatting.ALPHABETIC_KEY_ORDER).build();
            BuiltInRegistries.ITEM.holders().forEach(reference -> {
                net.minecraft.world.entity.EquipmentSlot slot = Mob.getEquipmentSlotForItem(new ItemStack(reference.value()));
                EquipmentSlot apiSlot = EquipmentSlot.values()[slot.ordinal()];
                if (apiSlot != this.defaultValue.object()) {
                    String formattedSlot = "%s.%s".formatted(EquipmentSlot.class.getSimpleName(), EquipmentSlot.values()[slot.ordinal()].name()); // name doesn't match
                    map.put(returnOf(apiSlot, formattedSlot), reference.key().location().getPath().toUpperCase(Locale.ENGLISH));
                }
            });
            return map;
        }
    }
}
