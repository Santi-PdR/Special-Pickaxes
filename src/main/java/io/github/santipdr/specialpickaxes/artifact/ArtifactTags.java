package io.github.santipdr.specialpickaxes.artifact;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** Datapack-extensible block tags used by the pickaxes' world interactions. */
public final class ArtifactTags {
    private ArtifactTags() {}

    public static final class Blocks {
        private Blocks() {}
        public static final TagKey<Block> THERMAL_SOURCES=TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(SpecialPickaxes.ID,"thermal_sources"));
    }
}
