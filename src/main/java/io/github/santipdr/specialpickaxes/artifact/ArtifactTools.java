package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public final class ArtifactTools {
    private ArtifactTools(){}
    public static boolean effective(BlockState s){return s.is(BlockTags.MINEABLE_WITH_PICKAXE)||s.is(BlockTags.MINEABLE_WITH_AXE)||s.is(BlockTags.MINEABLE_WITH_SHOVEL)
        ||s.is(net.minecraftforge.common.Tags.Blocks.ORES)||s.is(net.minecraftforge.common.Tags.Blocks.STONE)||s.is(net.minecraftforge.common.Tags.Blocks.GRAVEL)
        ||s.is(BlockTags.DIRT)||s.is(net.minecraft.world.level.block.Blocks.GRANITE)||s.is(net.minecraft.world.level.block.Blocks.DIORITE)
        ||s.is(net.minecraft.world.level.block.Blocks.ANDESITE)||s.is(net.minecraft.world.level.block.Blocks.TUFF)||s.is(net.minecraft.world.level.block.Blocks.CALCITE)
        ||s.is(net.minecraft.world.level.block.Blocks.DRIPSTONE_BLOCK)||s.is(net.minecraft.world.level.block.Blocks.POINTED_DRIPSTONE);}
    public static boolean action(ToolAction a){return ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(a)||ToolActions.DEFAULT_AXE_ACTIONS.contains(a)||ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(a);}
}
