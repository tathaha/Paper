package io.papermc.paper.registry;

import net.kyori.adventure.key.Keyed;
import org.bukkit.GameEvent;
import org.bukkit.block.Biome;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.ApiStatus;

import static io.papermc.paper.registry.RegistryKeyImpl.create;

/**
 * Identifier for a specific registry. For use with
 * {@link TypedKey} and the registry modification API.
 * <p>
 * There are 2 types of registries, identified as "built-in"
 * or "data-driven". The former are not changeable by datapacks (which
 * doesn't necessarily mean they aren't changeable in the API) and
 * are loaded first. "Data-driven" registries are all created by
 * reading in data from the vanilla and other datapacks.
 *
 * @param <T> the value type
 */
@SuppressWarnings("unused")
@ApiStatus.Experimental
public sealed interface RegistryKey<T> extends Keyed permits RegistryKeyImpl {

    /* ******************* *
     * Built-in Registries *
     * ******************* */
    /**
     * Built-in registry for game events
     * @see io.papermc.paper.registry.keys.GameEventKeys
     */
    RegistryKey<GameEvent> GAME_EVENT = create("game_event");
    /**
     * Built-in registry for structure types.
     * @see io.papermc.paper.registry.keys.StructureTypeKeys
     */
    RegistryKey<StructureType> STRUCTURE_TYPE = create("worldgen/structure_type");

    /* ********************** *
     * Data-driven Registries *
     * ********************** */
    /**
     * Data-driven registry for biomes.
     * @see io.papermc.paper.registry.keys.BiomeKeys
     */
    RegistryKey<Biome> BIOME = create("worldgen/biome");
    /**
     * Data-driven registry for structures.
     * @see io.papermc.paper.registry.keys.StructureKeys
     */
    RegistryKey<Structure> STRUCTURE = create("worldgen/structure");
    /**
     * Data-driven registry for trim materials.
     * @see io.papermc.paper.registry.keys.TrimMaterialKeys
     */
    RegistryKey<TrimMaterial> TRIM_MATERIAL = create("trim_material");
    /**
     * Data-driven registry for trim patterns.
     * @see io.papermc.paper.registry.keys.TrimPatternKeys
     */
    RegistryKey<TrimPattern> TRIM_PATTERN = create("trim_pattern");
}
