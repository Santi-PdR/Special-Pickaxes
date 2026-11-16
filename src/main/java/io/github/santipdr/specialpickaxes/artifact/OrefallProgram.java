package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Center-out Iridium extraction. Each loaded ore is mined as soon as it is reached. */
public final class OrefallProgram implements WorkProgram {
    public static final int RADIUS=24,HALF_HEIGHT=12;
    private static final List<BlockPos> SCAN_OFFSETS=createScanOffsets();
    private final BlockPos center;
    private final int limit;
    private int scanIndex,mined;

    /** Build the center-out order during mod setup instead of on the first ability activation. */
    public static void warmup(){SCAN_OFFSETS.size();}

    public OrefallProgram(BlockPos center){
        this.center=center.immutable();
        this.limit=Math.min(SCAN_OFFSETS.size(),ArtifactConfig.JOB_LIMIT.get());
    }

    /** Shared immutable plan; no per-cast cube allocation or synchronous world scan. */
    public static List<BlockPos> scanOffsets(){return SCAN_OFFSETS;}

    private static List<BlockPos> createScanOffsets(){
        var offsets=new ArrayList<BlockPos>();
        for(int x=-RADIUS;x<=RADIUS;x++)for(int y=-HALF_HEIGHT;y<=HALF_HEIGHT;y++)for(int z=-RADIUS;z<=RADIUS;z++)
            if(x*x+z*z+4*y*y<=RADIUS*RADIUS)offsets.add(new BlockPos(x,y,z));
        offsets.sort(Comparator.comparingDouble((BlockPos pos)->pos.distSqr(BlockPos.ZERO))
                .thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getZ));
        return List.copyOf(offsets);
    }

    @Override public int remaining(){return Math.max(0,SCAN_OFFSETS.size()-scanIndex);}
    @Override public boolean awaiting(){return false;}
    @Override public boolean executing(){return true;}
    @Override public boolean done(){return scanIndex>=SCAN_OFFSETS.size()||mined>=limit;}
    @Override public void confirm(){}
    @Override public boolean loaded(ServerPlayer p){return true;} // Unloaded chunks are skipped, never requested.
    @Override public boolean reportPartial(){return false;} // Empty and protected cells are expected scan results.
    @Override public int attemptsPerTick(ItemStack tool){
        return Math.max(1,ArtifactConfig.ORE_SCAN_BUDGET.get());
    }
    @Override public void reportProgress(ServerPlayer p){
        if(scanIndex>0&&scanIndex<SCAN_OFFSETS.size()&&p.tickCount%20==0)
            ArtifactFeedback.message(p,"ore_harvest_progress",scanIndex*100/SCAN_OFFSETS.size(),mined);
    }
    @Override public boolean backpressured(ServerPlayer p){
        if(done())return false;
        var pos=center.offset(SCAN_OFFSETS.get(scanIndex));
        if(!p.serverLevel().hasChunkAt(pos))return false;
        var state=p.serverLevel().getBlockState(pos);
        return ArtifactOres.isOre(state)&&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.IRIDIUM,pos,state);
    }
    @Override public WorkStep next(ServerPlayer p){
        if(done())return empty(center);
        var pos=center.offset(SCAN_OFFSETS.get(scanIndex++)).immutable();
        return new WorkStep(){
            @Override public BlockPos pos(){return pos;}
            @Override public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind kind){
                var level=actor.serverLevel();
                if(level.isOutsideBuildHeight(pos)||!level.hasChunkAt(pos))return false;
                BlockState state=level.getBlockState(pos);
                if(!ArtifactOres.isOre(state)||!WorldSafety.mineQueued(actor,tool,ArtifactKind.IRIDIUM,pos,state))return false;
                mined++;
                return true;
            }
        };
    }

    private static WorkStep empty(BlockPos pos){
        return new WorkStep(){
            @Override public BlockPos pos(){return pos;}
            @Override public boolean apply(ServerPlayer player,ItemStack tool,ArtifactKind kind){return false;}
        };
    }
}
