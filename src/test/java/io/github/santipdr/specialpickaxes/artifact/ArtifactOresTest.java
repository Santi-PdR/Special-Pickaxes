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
    @Test void dropRatesAndStorageBlocksNeverBecomeOreFamilies() {
        for(String path:new String[]{"ore_rates","ore_rates/dense","ore_rates_sparse","ore_storage_block",
                "ores_storage_blocks","ores_in_ground/stone","ore_bearing_ground/stone_hosts"}){
            assertFalse(ArtifactOres.isOreTagPath(path),path);
            assertFalse(ArtifactOres.isVeinFamilyTag(path),path);
        }
    }
    @Test void specificFamiliesConnectButAggregateOreTagsDoNot() {
        for(String path:new String[]{"ores/x","ore/x","ores/iron","copper_ores","ore_cobalt"})
            assertTrue(ArtifactOres.isVeinFamilyTag(path),path);
        for(String path:new String[]{"ore","ores","stone","",null})
            assertFalse(ArtifactOres.isVeinFamilyTag(path),path);
    }
}
