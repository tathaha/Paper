package io.papermc.generator;

import io.papermc.generator.rewriter.CompositeRewriter;
import io.papermc.generator.rewriter.SourceRewriter;
import io.papermc.generator.rewriter.types.EnumCloneRewriter;
import io.papermc.generator.rewriter.types.EnumRegistryRewriter;
import io.papermc.generator.rewriter.types.simple.MapPaletteRewriter;
import io.papermc.generator.rewriter.types.RegistryFieldRewriter;
import io.papermc.generator.rewriter.types.TagRewriter;
import io.papermc.generator.rewriter.types.simple.MaterialRewriter;
import io.papermc.generator.rewriter.types.simple.PatternTypeRewriter;
import io.papermc.generator.rewriter.types.simple.StatisticRewriter;
import io.papermc.generator.utils.ExperimentalSounds;
import io.papermc.generator.types.registry.GeneratedKeyType;
import io.papermc.generator.types.SourceGenerator;
import io.papermc.generator.types.goal.MobGoalGenerator;
import io.papermc.generator.utils.Formatting;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.bukkit.Art;
import org.bukkit.Fluid;
import org.bukkit.GameEvent;
import net.minecraft.world.item.Rarity;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Sniffer;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Villager;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.map.MapCursor;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Locale;

import static io.papermc.generator.utils.Formatting.quoted;

public interface Generators {

    SourceGenerator[] API = {
        simpleKey("GameEventKeys", GameEvent.class, Registries.GAME_EVENT, RegistryKey.GAME_EVENT, false),
        simpleKey("BiomeKeys", Biome.class, Registries.BIOME, RegistryKey.BIOME, true),
        simpleKey("TrimMaterialKeys", TrimMaterial.class, Registries.TRIM_MATERIAL, RegistryKey.TRIM_MATERIAL, true),
        simpleKey("TrimPatternKeys", TrimPattern.class, Registries.TRIM_PATTERN, RegistryKey.TRIM_PATTERN, true),
        simpleKey("StructureKeys", Structure.class, Registries.STRUCTURE, RegistryKey.STRUCTURE, true),
        simpleKey("StructureTypeKeys", StructureType.class, Registries.STRUCTURE_TYPE, RegistryKey.STRUCTURE_TYPE, false),
        simpleKey("InstrumentKeys", MusicInstrument.class, Registries.INSTRUMENT, RegistryKey.INSTRUMENT, false),
        simpleKey("EnchantmentKeys", Enchantment.class, Registries.ENCHANTMENT, RegistryKey.ENCHANTMENT, false),
        simpleKey("MobEffectKeys", PotionEffectType.class, Registries.MOB_EFFECT, RegistryKey.MOB_EFFECT, false),
        simpleKey("DamageTypeKeys", DamageType.class, Registries.DAMAGE_TYPE, RegistryKey.DAMAGE_TYPE, true),
        simpleKey("WolfVariantKeys", Wolf.Variant.class, Registries.WOLF_VARIANT, RegistryKey.WOLF_VARIANT, true),
        new MobGoalGenerator("VanillaGoal", "com.destroystokyo.paper.entity.ai"),
        /*new SoundGenerator("Sound", "org.bukkit"), todo extract fields
        new BiomeGenerator("Biome", "org.bukkit.block"),
        new AttributeGenerator("Attribute", "org.bukkit.attribute"),
        new StructureTypeGenerator("StructureType", "org.bukkit.generator.structure"),
        new StructureGenerator("Structure", "org.bukkit.generator.structure"),
        new LegacyKeyedRegistryGenerator<>("TrimPattern", "org.bukkit.inventory.meta.trim", Registries.TRIM_PATTERN, RegistryKey.TRIM_PATTERN),
        new LegacyKeyedRegistryGenerator<>("TrimMaterial", "org.bukkit.inventory.meta.trim", Registries.TRIM_MATERIAL, RegistryKey.TRIM_MATERIAL),
        new TagGenerator("Tag", "org.bukkit")*/
    };

    SourceRewriter[] API_REWRITE = {
        //new EnumCloneRewriter(Pose.class, net.minecraft.world.entity.Pose.class, "Pose", false)
        new EnumRegistryRewriter<>(Fluid.class, Registries.FLUID, "Fluid", false),
        new EnumRegistryRewriter<>(Sound.class, Registries.SOUND_EVENT, "Sound", true) {
            @Override
            protected String getExperimentalValue(Holder.Reference<SoundEvent> reference) {
                String result = super.getExperimentalValue(reference);
                if (result != null) {
                    return result;
                }
                return ExperimentalSounds.findExperimentalValue(reference.key().location());
            }
        },
        new EnumRegistryRewriter<>(Biome.class, Registries.BIOME, "Biome", false),
        new EnumRegistryRewriter<>(Frog.Variant.class, Registries.FROG_VARIANT, "FrogVariant", false),
        new EnumRegistryRewriter<>(Villager.Type.class, Registries.VILLAGER_TYPE, "VillagerType", false),
        new EnumRegistryRewriter<>(Attribute.class, Registries.ATTRIBUTE, "Attribute", true),
        new EnumRegistryRewriter<>(Cat.Type.class, Registries.CAT_VARIANT, "CatType", true),
        new EnumRegistryRewriter<>(PotionType.class, Registries.POTION, "PotionType", true),
        //new EnumRegistryRewriter<>(EntityType.class, Registries.ENTITY_TYPE, "EntityType", true), seems complex to get the typeId?
        new EnumRegistryRewriter<>(Art.class, Registries.PAINTING_VARIANT, "Art", true) {

            private static final int PIXELS_PER_BLOCK = 16;
            @Override
            protected String rewriteEnumValue(Holder.Reference<PaintingVariant> reference) {
                PaintingVariant variant = reference.value();
                return "%d, %d, %d".formatted(
                    BuiltInRegistries.PAINTING_VARIANT.getId(variant),
                    Mth.positiveCeilDiv(variant.getWidth(), PIXELS_PER_BLOCK),
                    Mth.positiveCeilDiv(variant.getHeight(), PIXELS_PER_BLOCK)
                );
            }
        },
        new PatternTypeRewriter("PatternType"),
        new EnumRegistryRewriter<>(MapCursor.Type.class, Registries.MAP_DECORATION_TYPE, "MapCursorType", true) {
            @Override
            protected String rewriteEnumValue(Holder.Reference<MapDecorationType> reference) {
                return "%d, %s".formatted(BuiltInRegistries.MAP_DECORATION_TYPE.getId(reference.value()), super.rewriteEnumValue(reference));
            }
        },
        new EnumCloneRewriter<>(DisplaySlot.class, net.minecraft.world.scores.DisplaySlot.class, "DisplaySlot", false) {
            @Override
            protected String rewriteEnumName(net.minecraft.world.scores.DisplaySlot slot) {
                if (slot == net.minecraft.world.scores.DisplaySlot.LIST) {
                    return "PLAYER_LIST";
                }

                return Formatting.formatKeyAsField(slot.getSerializedName());
            }

            @Override
            protected String rewriteEnumValue(net.minecraft.world.scores.DisplaySlot slot) {
                return quoted(slot.getSerializedName());
            }
        },
        new EnumCloneRewriter<>(Sniffer.State.class, net.minecraft.world.entity.animal.sniffer.Sniffer.State.class, "SnifferState", false),
        new EnumCloneRewriter<>(Panda.Gene.class, net.minecraft.world.entity.animal.Panda.Gene.class, "PandaGene", false) {
            @Override
            protected String rewriteEnumValue(net.minecraft.world.entity.animal.Panda.Gene gene) {
                return String.valueOf(gene.isRecessive());
            }
        },
        new EnumCloneRewriter<>(CookingBookCategory.class, net.minecraft.world.item.crafting.CookingBookCategory.class, "CookingBookCategory", false),
        new EnumCloneRewriter<>(CraftingBookCategory.class, net.minecraft.world.item.crafting.CraftingBookCategory.class, "CraftingBookCategory", false),
        new EnumCloneRewriter<>(TropicalFish.Pattern.class, net.minecraft.world.entity.animal.TropicalFish.Pattern.class, "TropicalFishPattern", false),
        new EnumCloneRewriter<>(Fox.Type.class, net.minecraft.world.entity.animal.Fox.Type.class, "FoxType", false),
        new EnumCloneRewriter<>(ItemRarity.class, Rarity.class, "ItemRarity", false) {
            @Override
            protected String rewriteEnumValue(final Rarity rarity) {
                return "%s.%s".formatted(NamedTextColor.class.getCanonicalName(), rarity.color().name());
            }
        },
        CompositeRewriter.bind(
            new EnumCloneRewriter<>(Boat.Type.class, net.minecraft.world.entity.vehicle.Boat.Type.class, "BoatType", false) {
                @Override
                protected String rewriteEnumValue(net.minecraft.world.entity.vehicle.Boat.Type type) {
                    return "%s.%s".formatted(Material.class.getSimpleName(), BuiltInRegistries.BLOCK.getKey(type.getPlanks()).getPath().toUpperCase(Locale.ENGLISH));
                }
            },
            new EnumCloneRewriter<>(Boat.Status.class, net.minecraft.world.entity.vehicle.Boat.Status.class, "BoatStatus", false)
        ),
        CompositeRewriter.bind(
            new MaterialRewriter.Blocks("Blocks"),
            //new MaterialRewriter.IsTransparent("Material#isTransparent"),

            new MaterialRewriter.Items("Items"),
            new MaterialRewriter.GetEquipmentSlot("Material#getEquipmentSlot")
        ),
        CompositeRewriter.bind(
            new StatisticRewriter.Custom("StatisticCustom"),
            new StatisticRewriter.Type("StatisticType")
        ),
        new RegistryFieldRewriter<>(Structure.class, Registries.STRUCTURE, "Structure", "getStructure"),
        new RegistryFieldRewriter<>(StructureType.class, Registries.STRUCTURE_TYPE, "StructureType", "getStructureType"),
        new RegistryFieldRewriter<>(TrimPattern.class, Registries.TRIM_PATTERN, "TrimPattern", null),
        new RegistryFieldRewriter<>(TrimMaterial.class, Registries.TRIM_MATERIAL, "TrimMaterial", null),
        new RegistryFieldRewriter<>(DamageType.class, Registries.DAMAGE_TYPE, "DamageType", "getDamageType"),
        new RegistryFieldRewriter<>(GameEvent.class, Registries.GAME_EVENT, "GameEvent", "getEvent"),
        new RegistryFieldRewriter<>(MusicInstrument.class, Registries.INSTRUMENT, "MusicInstrument", "getInstrument"),
        new RegistryFieldRewriter<>(Wolf.Variant.class, Registries.WOLF_VARIANT, "WolfVariant", "getVariant"),
        new TagRewriter(Tag.class, "Tag"),
        new MapPaletteRewriter("MapPalette#colors"),
    };

    private static <T, A> SourceGenerator simpleKey(final String className, final Class<A> apiType, final ResourceKey<? extends Registry<T>> registryKey, final RegistryKey<A> apiRegistryKey, final boolean publicCreateKeyMethod) {
        return new GeneratedKeyType<>(className, apiType, "io.papermc.paper.registry.keys", registryKey, apiRegistryKey, publicCreateKeyMethod);
    }
}
