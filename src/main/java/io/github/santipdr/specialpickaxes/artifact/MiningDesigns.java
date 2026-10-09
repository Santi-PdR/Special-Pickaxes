package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

/** Mining-specific plans. Resource detection never sends ore positions or outlines to the client. */
public final class MiningDesigns {
    private MiningDesigns(){}
    public static boolean drill(ServerPlayer p,ItemStack tool,ArtifactKind k){
        var look=p.getLookAngle();var direction=Direction.getNearest(look.x,look.y,look.z);
        int mode=ArtifactState.mode(p,k);
        var shape=k==ArtifactKind.CHOIR?DirectionalProgram.Shape.RESONANT_TUNNEL:k==ArtifactKind.ICARUS?(mode==1?DirectionalProgram.Shape.ICARUS_WIDE:DirectionalProgram.Shape.ICARUS):k==ArtifactKind.EXODIUM?DirectionalProgram.Shape.EXODIUM_LANCE:DirectionalProgram.Shape.values()[mode];
        var origin=k==ArtifactKind.CHOIR||k==ArtifactKind.ICARUS||k==ArtifactKind.EXODIUM?p.blockPosition().above().relative(direction):shape==DirectionalProgram.Shape.CORE_DRILL?p.blockPosition().below():ArtifactActions.target(p).orElse(p.blockPosition().relative(direction,2));
        return WorkQueue.startRegion(p,tool,k,new DirectionalProgram(origin,direction,shape,k));
    }
    public static boolean rootwake(ServerPlayer p,ItemStack tool,BlockPos origin){
        var level=p.serverLevel();var state=level.getBlockState(origin);
        if(!MiningDesigns.matrix(state)||ArtifactOres.isOre(state)||PlayerPlacedBlocks.get(level).contains(origin)
                ||!WorldSafety.allowed(p,ArtifactKind.WORLDLOOM,origin)||!WorldSafety.harvestable(p,tool,origin))return false;
        Direction direction=Direction.getNearest(p.getLookAngle().x,p.getLookAngle().y,p.getLookAngle().z);
        return WorkQueue.startRegion(p,tool,ArtifactKind.WORLDLOOM,
                new DirectionalProgram(origin,direction,DirectionalProgram.Shape.ROOTWAKE,ArtifactKind.WORLDLOOM));
    }
    public static boolean orefall(ServerPlayer p,ItemStack tool,BlockPos center){
        int radius=8,halfHeight=4;var candidates=new ArrayList<BlockPos>();var level=p.serverLevel();
        for(int x=-radius;x<=radius;x++)for(int y=-halfHeight;y<=halfHeight;y++)for(int z=-radius;z<=radius;z++){
            if(x*x+z*z+4*y*y>radius*radius)continue;var pos=center.offset(x,y,z);
            if(level.hasChunkAt(pos)&&ArtifactOres.isOre(level.getBlockState(pos))
                    &&WorldSafety.allowed(p,ArtifactKind.IRIDIUM,pos)&&WorldSafety.harvestable(p,tool,pos))candidates.add(pos.immutable());
        }
        candidates.sort(java.util.Comparator.comparingDouble(pos->pos.distSqr(center)));
        var steps=new ArrayList<WorkStep>(Math.min(candidates.size(),128));
        for(int i=0;i<candidates.size()&&i<128;i++)steps.add(new WorkStep.Mine(candidates.get(i),level.getBlockState(candidates.get(i))));
        return WorkQueue.start(p,tool,ArtifactKind.IRIDIUM,steps);
    }
    public static boolean matrix(net.minecraft.world.level.block.state.BlockState s){return s.is(net.minecraftforge.common.Tags.Blocks.STONE)||s.is(net.minecraft.tags.BlockTags.DIRT)||s.is(net.minecraftforge.common.Tags.Blocks.GRAVEL)||s.is(Blocks.GRANITE)||s.is(Blocks.DIORITE)||s.is(Blocks.ANDESITE)||s.is(Blocks.DEEPSLATE)||s.is(Blocks.COBBLESTONE)||s.is(Blocks.COBBLED_DEEPSLATE)||s.is(Blocks.BLACKSTONE)||s.is(Blocks.POLISHED_BLACKSTONE)||s.is(Blocks.NETHERRACK)||s.is(Blocks.END_STONE)||s.is(Blocks.TUFF)||s.is(Blocks.CALCITE)||s.is(Blocks.BASALT)||s.is(Blocks.SMOOTH_BASALT)||s.is(Blocks.OBSIDIAN)||s.is(Blocks.DRIPSTONE_BLOCK)||s.is(Blocks.POINTED_DRIPSTONE);}
    public static boolean seamGeology(net.minecraft.world.level.block.state.BlockState s){return matrix(s)&&!ArtifactOres.isOre(s)&&WorldSafety.inert(s);}
    public static boolean crucibleGeology(net.minecraft.world.level.block.state.BlockState s){
        if(s.hasBlockEntity()||!s.getFluidState().isEmpty()||ArtifactOres.isOre(s))return false;
        if(s.is(net.minecraftforge.common.Tags.Blocks.STONE)||s.is(net.minecraft.tags.BlockTags.DIRT)||s.is(net.minecraftforge.common.Tags.Blocks.GRAVEL))return true;
        var block=s.getBlock();return block==Blocks.STONE||block==Blocks.COBBLESTONE||block==Blocks.DEEPSLATE||block==Blocks.COBBLED_DEEPSLATE
            ||block==Blocks.GRANITE||block==Blocks.DIORITE||block==Blocks.ANDESITE||block==Blocks.TUFF||block==Blocks.CALCITE
            ||block==Blocks.BLACKSTONE||block==Blocks.POLISHED_BLACKSTONE
            ||block==Blocks.BASALT||block==Blocks.SMOOTH_BASALT||block==Blocks.OBSIDIAN||block==Blocks.NETHERRACK||block==Blocks.END_STONE
            ||block==Blocks.DIRT||block==Blocks.COARSE_DIRT||block==Blocks.GRASS_BLOCK||block==Blocks.PODZOL||block==Blocks.ROOTED_DIRT||block==Blocks.GRAVEL
            ||block==Blocks.DRIPSTONE_BLOCK||block==Blocks.POINTED_DRIPSTONE;
    }
}
