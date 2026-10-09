package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Mines a protected, player-sized shaft upward, raising its owner one block at a time. */
public final class IcarianLiftProgram implements WorkProgram {
    private static final int MAX_RISE=96;
    private final BlockPos base;
    private int rise,index;
    private boolean finished;

    public IcarianLiftProgram(BlockPos base){this.base=base.immutable();}

    private int cells(){return rise==0?2:1;}
    private BlockPos nextPos(){
        int layer=rise==0?1+index:rise+2;
        return base.above(layer);
    }

    @Override public int remaining(){return finished?0:Math.max(0,MAX_RISE-rise)+cells()-index;}
    @Override public boolean awaiting(){return false;}
    @Override public boolean executing(){return true;}
    @Override public boolean done(){return finished;}
    @Override public void confirm(){}
    @Override public boolean loaded(ServerPlayer p){return finished||index>=cells()||p.serverLevel().hasChunkAt(nextPos());}

    @Override public boolean backpressured(ServerPlayer p){
        if(finished||index>=cells())return false;
        BlockPos pos=nextPos();var level=p.serverLevel();
        if(!level.hasChunkAt(pos))return false;
        var state=level.getBlockState(pos);
        return !state.isAir()&&!state.is(net.minecraft.world.level.block.Blocks.CAVE_AIR)
                &&!state.is(net.minecraft.world.level.block.Blocks.VOID_AIR)
                &&!PlayerPlacedBlocks.get(level).contains(pos)&&!WorldSafety.barrier(p,pos)
                &&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.ICARUS,pos,state);
    }

    @Override public WorkStep next(ServerPlayer p){
        if(finished)return completed(base);
        if(index>=cells()){
            if(rise>=MAX_RISE){finished=true;return completed(base.offset(0,rise,0));}
            int nextRise=rise+1;
            boolean reachedSky=p.serverLevel().canSeeSky(base.offset(0,nextRise+1,0));
            rise=nextRise;index=0;finished=reachedSky;
            return new WorkStep.Move(base.offset(0,nextRise,0));
        }

        BlockPos pos=nextPos();index++;
        var level=p.serverLevel();BlockState state=level.getBlockState(pos);
        if(state.isAir()||state.is(net.minecraft.world.level.block.Blocks.CAVE_AIR)
                ||state.is(net.minecraft.world.level.block.Blocks.VOID_AIR))return completed(pos);
        if(PlayerPlacedBlocks.get(level).contains(pos)||WorldSafety.barrier(p,pos)
                ||!WorldSafety.harvestable(p,p.getMainHandItem(),pos)){
            finished=true;
            return completed(pos);
        }
        return new WorkStep(){
            @Override public BlockPos pos(){return pos;}
            @Override public boolean stopOnFailure(){return true;}
            @Override public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind kind){
                return !PlayerPlacedBlocks.get(actor.serverLevel()).contains(pos)
                        &&WorldSafety.mineQueued(actor,tool,ArtifactKind.ICARUS,pos,state);
            }
        };
    }

    private static WorkStep completed(BlockPos pos){
        return new WorkStep(){
            @Override public BlockPos pos(){return pos;}
            @Override public boolean apply(ServerPlayer player,ItemStack tool,ArtifactKind kind){return true;}
        };
    }
}
