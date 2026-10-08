package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArtifactOresTest {
    @Test void recognizesConventionalOreFamilyTagsWithoutMatchingGroundTags() {
        assertTrue(ArtifactOres.isOreTagPath("ores"));
        assertTrue(ArtifactOres.isOreTagPath("ores/iron"));
        assertTrue(ArtifactOres.isOreTagPath("ore/cobalt"));
        assertTrue(ArtifactOres.isOreTagPath("copper_ores"));
        assertFalse(ArtifactOres.isOreTagPath("ores_in_ground/deepslate"));
        assertFalse(ArtifactOres.isOreTagPath("stone"));
    }
}
