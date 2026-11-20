package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Large alternate excavations with constant plan memory and scheduler-budgeted world reads. */
public final class NaturalExcavationProgram implements WorkProgram {
    private final BlockPos origin;
    private final Direction forward;
    private final ExcavationVolume.Shape shape;
    private final ArtifactKind kind;
    private final int limit;
    private int index;

    public NaturalExcavationProgram(BlockPos origin,Direction forward,ExcavationVolume.Shape shape){
        if(forward.getAxis()==Direction.Axis.Y)throw new IllegalArgumentException("horizontal excavation direction required");
        this.origin=origin.immutable();this.forward=forward;this.shape=shape;
        kind=shape==ExcavationVolume.Shape.VAULT?ArtifactKind.CHOIR:ArtifactKind.HELLSPEC;
        limit=Math.min(shape.volume,ArtifactConfig.JOB_LIMIT.get());
    }
    public static boolean eligible(BlockState state){return !state.isAir()&&!state.hasBlockEntity()&&state.getFluidState().isEmpty()
            &&state.getBlock().defaultDestroyTime()>=0;}
    private BlockPos at(int cell){return ExcavationVolume.cell(origin,forward,shape,cell);}
    public int remaining(){return Math.max(0,limit-index);}
    public boolean awaiting(){return false;}
    public boolean executing(){return true;}
    public boolean done(){return index>=limit;}
    public void confirm(){}
    public boolean loaded(ServerPlayer player){return true;} // Skip unloaded cells; never request chunks.
    public boolean reportPartial(){return false;} // Air and protected cells are expected omissions.
    public boolean backpressured(ServerPlayer player){
        if(done())return false;
        var pos=at(index);var level=player.serverLevel();
        if(level.isOutsideBuildHeight(pos)||!level.hasChunkAt(pos)||PlayerPlacedBlocks.get(level).contains(pos))return false;
        var state=level.getBlockState(pos);
        return eligible(state)&&WorldSafety.backpressuredMine(player,player.getMainHandItem(),kind,pos,state);
    }
    public WorkStep next(ServerPlayer player){
        if(done())throw new IllegalStateException("excavation exhausted");
        var pos=at(index++);
        return new WorkStep(){
            public BlockPos pos(){return pos;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                var level=actor.serverLevel();
                if(level.isOutsideBuildHeight(pos)||!level.hasChunkAt(pos)||PlayerPlacedBlocks.get(level).contains(pos))return false;
                var state=level.getBlockState(pos);
                return eligible(state)&&WorldSafety.mineNaturalQueued(actor,tool,kind,pos,state);
            }
        };
    }
}
