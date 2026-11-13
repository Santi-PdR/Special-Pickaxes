package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Registry-free rules for inferring modded geology from block ids. */
class GeologyNamesTest {
    @Test void wholeSegmentsMatchGeologyButFusedWordsDoNot() {
        assertTrue(GeologyNames.geologyPart("chiseled_dripstone"));
        assertTrue(GeologyNames.geologyPart("igneous_basalt"));
        assertTrue(GeologyNames.geologyPart("stellar_remnant_slate"));
        assertFalse(GeologyNames.geologyPart("lodestone"));
        assertFalse(GeologyNames.geologyPart("milestone"));
        assertFalse(GeologyNames.geologyPart("stonecutter"));
    }

    @Test void craftedShapesAreNotTreatedAsTerrain() {
        for (String path : new String[]{"moonstone_slab", "moonstone_stairs", "igneostone_brick_wall", "granite_trapdoor",
                "deepslate_door", "calcite_pillar", "basalt_fence_gate", "marble_window"}) {
            assertTrue(GeologyNames.decoration(path), path);
        }
        for (String path : new String[]{"moonstone", "igneostone", "dripstone_block", "basalt", "slate_rock"}) {
            assertFalse(GeologyNames.decoration(path), path);
        }
    }

    @Test void inferenceAppliesOnlyToOtherNamespaces() {
        assertTrue(GeologyNames.inferred("terramity", "igneous_basalt", false));
        assertFalse(GeologyNames.inferred("minecraft", "basalt", true), "vanilla geology keeps its explicit list");
        assertFalse(GeologyNames.inferred("terramity", "moonstone_slab", false), "structures never infer as geology");
        assertFalse(GeologyNames.inferred("", "", false));
    }
}
