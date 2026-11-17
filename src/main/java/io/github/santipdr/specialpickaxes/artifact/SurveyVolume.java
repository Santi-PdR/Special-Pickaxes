package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/** Nearest-first survey geometry; finishes a complete distance tie before stopping. */
public final class SurveyVolume {
    public static final int RADIUS=6,LIMIT=32;
    private static final List<BlockPos> OFFSETS=createOffsets();
    private SurveyVolume(){}
    public static void warmup(){OFFSETS.size();}
    public static List<BlockPos> offsets(){return OFFSETS;}
    private static List<BlockPos> createOffsets(){
        var offsets=new ArrayList<BlockPos>();
        for(int x=-RADIUS;x<=RADIUS;x++)for(int y=-RADIUS;y<=RADIUS;y++)for(int z=-RADIUS;z<=RADIUS;z++)offsets.add(new BlockPos(x,y,z));
        offsets.sort(Comparator.comparingDouble((BlockPos pos)->pos.distSqr(BlockPos.ZERO)));
        return List.copyOf(offsets);
    }
    /** Predicate is read-only and must not retain the reused mutable cursor. Results are immutable snapshots. */
    public static List<BlockPos> select(BlockPos origin,Predicate<BlockPos> eligible){
        var center=origin.immutable();
        var best=new NearestCandidates<BlockPos>(LIMIT,Comparator.comparingDouble((BlockPos pos)->pos.distSqr(center)).thenComparingLong(BlockPos::asLong));
        var cursor=new BlockPos.MutableBlockPos();
        int found=0,cutoff=Integer.MAX_VALUE;
        for(var offset:OFFSETS){
            int distance=(int)offset.distSqr(BlockPos.ZERO);
            if(distance>cutoff)break;
            cursor.set(center).move(offset);
            if(!eligible.test(cursor))continue;
            best.offer(cursor.immutable());
            if(++found>=LIMIT)cutoff=distance;
        }
        return best.sorted();
    }
}
