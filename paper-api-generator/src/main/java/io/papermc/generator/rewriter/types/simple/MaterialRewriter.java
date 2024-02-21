package io.papermc.generator.rewriter.types.simple;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import io.papermc.generator.rewriter.types.SwitchRewriter;
import io.papermc.generator.utils.Formatting;
import java.util.Comparator;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

@Deprecated(forRemoval = true)
public class MaterialRewriter {

    // blocks

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
