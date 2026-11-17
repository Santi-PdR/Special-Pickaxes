package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Center-out execution over shared immutable geometry; no per-cast counting or shell search. */
public final class RadialMiningProgram implements WorkProgram {
    private final BlockPos center;
    private final ArtifactKind kind;
    private final int scanLimit;
    private final BlockState target;
    private final java.util.List<BlockPos> offsets;
    private int scanned;

    public RadialMiningProgram(BlockPos center,int radius,ArtifactKind kind,int mode,BlockState target){
        this.center=center.immutable();this.kind=kind;this.target=target;
        var shape=switch(kind){
            case WORLDLOOM,CRUCIBLE -> RadialVolume.Shape.SPHERE;
            case HELLSPEC -> RadialVolume.Shape.BLOOM;
            case AXIOM -> RadialVolume.Shape.SHELL;
            default -> RadialVolume.Shape.CUBE;
        };
        offsets=RadialVolume.offsets(shape,radius);
        scanLimit=Math.min(ArtifactConfig.JOB_LIMIT.get(),offsets.size());
    }

    public BlockPos center(){return center;}
    private BlockPos peek(){return done()?null:center.offset(offsets.get(scanned));}

    @Override public int remaining(){return Math.max(0,scanLimit-scanned);}
    @Override public boolean awaiting(){return false;}
    @Override public boolean executing(){return true;}
    @Override public boolean done(){return scanned>=scanLimit;}
    @Override public void confirm(){}
    @Override public boolean loaded(ServerPlayer p){return true;} // Unloaded positions are skipped without requesting chunks.
    @Override public boolean reportPartial(){return false;} // Empty, protected and non-geological cells are normal scan results.

    @Override public boolean backpressured(ServerPlayer p){
        if(kind==ArtifactKind.CRUCIBLE)return false;
        BlockPos pos=peek();if(pos==null||!p.serverLevel().hasChunkAt(pos))return false;
        var state=p.serverLevel().getBlockState(pos);
        if(PlayerPlacedBlocks.get(p.serverLevel()).contains(pos)||!mineableGeology(state))return false;
        return WorldSafety.backpressuredMine(p,p.getMainHandItem(),kind,pos,state);
    }

    @Override public WorkStep next(ServerPlayer p){
        BlockPos pos=peek();
        if(pos==null)return empty(center);
        scanned++;
        if(kind==ArtifactKind.CRUCIBLE)return transmuteAt(pos,p);
        return mineAt(pos);
    }

    private WorkStep mineAt(BlockPos pos){
        return new WorkStep(){
            @Override public BlockPos pos(){return pos;}
            @Override public boolean apply(ServerPlayer player,ItemStack tool,ArtifactKind ignored){
                if(!player.serverLevel().hasChunkAt(pos))return false;
                BlockState state=player.serverLevel().getBlockState(pos);
                if(PlayerPlacedBlocks.get(player.serverLevel()).contains(pos)||!mineableGeology(state))return false;
                return WorldSafety.mineNaturalQueued(player,tool,kind,pos,state);
            }
        };
    }

    /** Ordinary radial geology preserves every ore; Hellspec deliberately mines ores too. */
    private boolean mineableGeology(BlockState state){
        if(kind==ArtifactKind.HELLSPEC&&ArtifactOres.isOre(state))return true;
        return MiningDesigns.matrix(state)&&!ArtifactOres.isOre(state);
    }

    private WorkStep transmuteAt(BlockPos pos,ServerPlayer player){
        if(target==null||!player.serverLevel().hasChunkAt(pos))return empty(pos);
        BlockState old=player.serverLevel().getBlockState(pos);
        if(old.getBlock()==target.getBlock()||!WorldSafety.crucibleSource(player,pos,old))return empty(pos);
        return new WorkStep.Rephase(pos,old,target);
    }

    private static WorkStep empty(BlockPos at){
        return new WorkStep(){
            @Override public BlockPos pos(){return at;}
            @Override public boolean apply(ServerPlayer player,ItemStack tool,ArtifactKind kind){return false;}
        };
    }
}
