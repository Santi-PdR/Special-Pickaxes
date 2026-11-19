package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Two rising, swept wings of natural excavation. No Move steps and no player position writes. */
public final class IcarianWingcut implements WorkProgram {
    public static final int LENGTH=32;
    private final List<BlockPos> cells;
    private final int limit;
    private int index;
    public IcarianWingcut(BlockPos origin,Direction forward,boolean wide){
        cells=geometry(origin,forward,wide);limit=Math.min(cells.size(),ArtifactConfig.JOB_LIMIT.get());
    }
    public static List<BlockPos> geometry(BlockPos origin,Direction forward,boolean wide){
        if(forward.getAxis().isVertical())throw new IllegalArgumentException("horizontal wing orientation");
        var result=new ArrayList<BlockPos>();var right=forward.getClockWise();
        for(int depth=0;depth<LENGTH;depth++){
            int span=Math.min(wide?24:18,4+depth/(wide?1:2));
            for(int side=-span;side<=span;side++)for(int thickness=-2;thickness<=2;thickness++)
                result.add(origin.relative(forward,depth).relative(right,side).above(Math.abs(side)/2+thickness));
        }
        return List.copyOf(result);
    }
    public int remaining(){return limit-index;}
    public boolean awaiting(){return false;}
    public boolean executing(){return true;}
    public boolean done(){return index>=limit;}
    public void confirm(){}
    public boolean loaded(ServerPlayer p){return true;}
    public boolean reportPartial(){return false;}
    public boolean backpressured(ServerPlayer p){
        if(done())return false;var at=cells.get(index);var level=p.serverLevel();
        if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
        var state=level.getBlockState(at);
        return NaturalExcavationProgram.eligible(state)&&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.ICARUS,at,state);
    }
    public WorkStep next(ServerPlayer p){
        if(done())throw new IllegalStateException("wingcut exhausted");
        var at=cells.get(index++);
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                var level=actor.serverLevel();if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
                var state=level.getBlockState(at);
                return NaturalExcavationProgram.eligible(state)&&WorldSafety.mineNaturalQueued(actor,tool,ArtifactKind.ICARUS,at,state);
            }
        };
    }
}
