package io.github.santipdr.specialpickaxes.artifact;

import java.util.Locale;
import java.util.Set;

/**
 * Registry-independent rules for inferring geology from a block id.
 *
 * Kept free of Minecraft statics so it can be unit tested without bootstrapping
 * registries. MiningDesigns applies these rules only after the hard protections
 * (ores, block entities, fluids) have already excluded a block, and after the
 * datapack allowlist tag has had its chance first.
 */
public final class GeologyNames {
    private GeologyNames() {}

    /** Tokens that describe rock, soil or sediment when they appear as a separate id segment. */
    private static final Set<String> GEOLOGY_PARTS = Set.of(
            "stone", "rock", "rocks", "basalt", "granite", "diorite", "andesite", "limestone", "marble", "slate", "shale",
            "tuff", "calcite", "dripstone", "speleothem", "stalactite", "stalagmite", "deepslate", "blackstone", "netherrack",
            "endstone", "end_stone", "obsidian", "sandstone", "cobble", "cobblestone", "gravel", "dirt", "clay",
            "gabbro", "gneiss", "quartzite", "schist", "phyllite", "argillite", "hornfels", "siltstone", "mudstone",
            "claystone", "conglomerate", "breccia", "pumice", "rhyolite", "dacite", "travertine", "dolomite", "chalk",
            "soapstone", "gypsum", "mud", "soil", "permafrost", "scoria");

    /**
     * Shape suffixes that always describe a crafted or structural piece rather than
     * terrain. Generated villages and mods place these, so transmuting them would be
     * destructive even when the base material is rock.
     */
    private static final Set<String> NON_GEOLOGY_SUFFIXES = Set.of(
            "trapdoor", "door", "slab", "stairs", "wall", "fence", "fence_gate", "gate", "button", "pressure_plate",
            "sign", "hanging_sign", "banner", "carpet", "pane", "ladder", "rail", "torch", "lantern", "chain",
            "bars", "bed", "candle", "window", "lamp", "grille", "grate", "post", "beam", "pillar");

    /** A block id that ends in a furniture/structure suffix is never treated as terrain. */
    public static boolean decoration(String path) {
        if (path == null || path.isEmpty()) return false;
        String lower = path.toLowerCase(Locale.ROOT);
        for (String suffix : NON_GEOLOGY_SUFFIXES) {
            if (lower.endsWith("_" + suffix) || lower.equals(suffix)) return true;
        }
        return false;
    }

    /** A geological token must be a whole id segment; fused words like "lodestone" never match. */
    public static boolean geologyPart(String path) {
        if (path == null || path.isEmpty()) return false;
        for (String part : path.toLowerCase(Locale.ROOT).split("_")) {
            if (GEOLOGY_PARTS.contains(part)) return true;
        }
        return false;
    }

    /** Name-based geology inference for a block that already passed the hard exclusions. */
    public static boolean inferred(String namespace, String path, boolean vanillaNamespace) {
        if (namespace == null || path == null || namespace.isEmpty()) return false;
        if (vanillaNamespace) return false; // vanilla geology stays on its explicit list
        if (decoration(path)) return false;
        return geologyPart(path);
    }
}
