package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Streaming 11-cubed temporal excavation: bounded memory, normal drops and execution-time permissions. */
public final class TemporalRiftProgram implements WorkProgram {
    public static final int RADIUS=5,DIAMETER=11,VOLUME=1331;
    private final BlockPos center;
    private final ArtifactKind kind;
    private final boolean harvestOres;
    private int index;
    public TemporalRiftProgram(BlockPos center,ArtifactKind kind,boolean harvestOres){
        this.center=center.immutable();this.kind=kind;this.harvestOres=harvestOres;
    }
    public static BlockPos cell(BlockPos center,int index){
        if(index<0||index>=VOLUME)throw new IllegalArgumentException("rift cell");
        // Center-first traversal avoids waiting through the rear corner before the target is affected.
        if(index==0)return center.immutable();
        int linear=index-1,mid=VOLUME/2;
        if(linear>=mid)linear++;
        return center.offset(linear/(DIAMETER*DIAMETER)-RADIUS,(linear/DIAMETER)%DIAMETER-RADIUS,linear%DIAMETER-RADIUS);
    }
    public int remaining(){return VOLUME-index;}
    public boolean awaiting(){return false;}public boolean executing(){return true;}public boolean done(){return index>=VOLUME;}
    public void confirm(){}public boolean loaded(ServerPlayer player){return true;}
    public boolean reportPartial(){return false;}
    public boolean backpressured(ServerPlayer player){
        if(done())return false;var at=cell(center,index);var level=player.serverLevel();
        if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at))return false;
        var state=level.getBlockState(at);
        return (MiningDesigns.matrix(state)||harvestOres&&ArtifactOres.isOre(state))
                &&!PlayerPlacedBlocks.get(level).contains(at)
                &&WorldSafety.backpressuredMine(player,player.getMainHandItem(),kind,at,state);
    }
    public WorkStep next(ServerPlayer player){
        if(done())throw new IllegalStateException("rift exhausted");
        var at=cell(center,index++);
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                var level=actor.serverLevel();
                if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
                var state=level.getBlockState(at);
                if(!MiningDesigns.matrix(state)&&!(harvestOres&&ArtifactOres.isOre(state)))return false;
                return WorldSafety.mineNaturalQueued(actor,tool,kind,at,state);
            }
        };
    }
}
