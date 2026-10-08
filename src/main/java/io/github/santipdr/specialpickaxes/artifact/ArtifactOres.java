package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

/** Vanilla ore families plus Forge's conventional tag for modded ores. */
public final class ArtifactOres {
    private ArtifactOres() {}
    public static boolean isOre(BlockState state){
        return state.is(net.minecraftforge.common.Tags.Blocks.ORES)||state.is(BlockTags.COAL_ORES)||state.is(BlockTags.IRON_ORES)
            ||state.is(BlockTags.GOLD_ORES)||state.is(BlockTags.DIAMOND_ORES)||state.is(BlockTags.REDSTONE_ORES)
            ||state.is(BlockTags.LAPIS_ORES)||state.is(BlockTags.EMERALD_ORES)||state.is(BlockTags.COPPER_ORES);
    }
}
