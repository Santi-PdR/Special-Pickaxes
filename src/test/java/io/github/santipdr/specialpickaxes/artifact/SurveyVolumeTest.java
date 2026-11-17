package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class SurveyVolumeTest {
    private static List<BlockPos> exhaustive(BlockPos origin,Set<BlockPos> ores){
        return ores.stream().filter(pos->Math.abs(pos.getX()-origin.getX())<=6&&Math.abs(pos.getY()-origin.getY())<=6&&Math.abs(pos.getZ()-origin.getZ())<=6)
                .sorted(Comparator.comparingDouble((BlockPos pos)->pos.distSqr(origin)).thenComparingLong(BlockPos::asLong)).limit(32).toList();
    }
    @Test void denseSurveyInspects33CellsInsteadOf2197AndRetainsWorldCoordinateTies(){
        for(var origin:List.of(BlockPos.ZERO,new BlockPos(-1,80,-1),new BlockPos(0,-1,0),new BlockPos(-6,100,6))){
            var all=new HashSet<BlockPos>();for(var offset:SurveyVolume.offsets())all.add(origin.offset(offset));
            var visited=new AtomicInteger();
            var actual=SurveyVolume.select(origin,pos->{visited.incrementAndGet();return all.contains(pos);});
            assertEquals(exhaustive(origin,all),actual);
            assertEquals(33,visited.get());
            assertThrows(UnsupportedOperationException.class,()->actual.add(origin));
        }
    }
    @Test void randomizedSparseAndDenseResultsExactlyMatchFullCubeSearch(){
        var random=new Random(78193);
        for(int trial=0;trial<80;trial++){
            var origin=new BlockPos(random.nextInt(40)-20,random.nextInt(300)-60,random.nextInt(40)-20);
            var ores=new HashSet<BlockPos>();
            for(var offset:SurveyVolume.offsets())if(random.nextInt(100)<trial)ores.add(origin.offset(offset));
            assertEquals(exhaustive(origin,ores),SurveyVolume.select(origin,ores::contains));
        }
    }
    @Test void emptySurveyVisitsEntireUniqueCubeAndDoesNotMutateOrigin(){
        var origin=new BlockPos.MutableBlockPos(-20,80,30);var visited=new HashSet<BlockPos>();
        assertTrue(SurveyVolume.select(origin,pos->{assertTrue(visited.add(pos.immutable()));return false;}).isEmpty());
        assertEquals(2197,visited.size());assertEquals(new BlockPos(-20,80,30),origin);
        assertSame(SurveyVolume.offsets(),SurveyVolume.offsets());
        assertThrows(UnsupportedOperationException.class,()->SurveyVolume.offsets().clear());
    }
}
