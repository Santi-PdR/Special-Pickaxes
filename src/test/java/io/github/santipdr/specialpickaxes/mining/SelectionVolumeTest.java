package io.github.santipdr.specialpickaxes.mining;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class SelectionVolumeTest {
    @Test void inclusiveSmallMediumLarge(){for(int n:new int[]{1,9,32,64}){var v=new SelectionVolume(BlockPos.ZERO,new BlockPos(n-1,n-1,n-1));assertEquals((long)n*n*n,v.size());assertEquals(v.max(),v.at(v.size()-1));}}
    @Test void fiftyThousandCellTraversalHasNoGaps(){var v=new SelectionVolume(BlockPos.ZERO,new BlockPos(49,19,49));var cells=new HashSet<BlockPos>();for(long i=0;i<v.size();i++)assertTrue(cells.add(v.at(i)));assertEquals(50000,cells.size());assertTrue(cells.contains(v.max()));}
    @Test void overflowSaturates(){var v=new SelectionVolume(new BlockPos(Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE),new BlockPos(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE));assertEquals(Long.MAX_VALUE,v.size());}
    @Test void allTransformsAreBijections(){var a=new SelectionVolume(BlockPos.ZERO,new BlockPos(8,2,0));for(var t:SelectionVolume.Transform.values()){boolean turn=t==SelectionVolume.Transform.ROTATE_90||t==SelectionVolume.Transform.ROTATE_270;var b=new SelectionVolume(new BlockPos(20,0,0),new BlockPos(turn?20:28,2,turn?8:0));var visited=new HashSet<BlockPos>();for(long i=0;i<a.size();i++){var q=t.map(a,b,a.at(i));assertTrue(b.contains(q));assertTrue(visited.add(q));}assertEquals(27,visited.size());}}
    @Test void incompatibleShapesRejected(){var a=new SelectionVolume(BlockPos.ZERO,new BlockPos(8,2,0));var b=new SelectionVolume(new BlockPos(20,0,0),new BlockPos(28,2,0));assertFalse(SelectionVolume.Transform.ROTATE_90.compatible(a,b));assertThrows(IllegalArgumentException.class,()->SelectionVolume.Transform.ROTATE_90.map(a,b,a.min()));assertFalse(a.overlaps(b));assertTrue(a.overlaps(a));}
    @Test void highEnchantmentsRemainFiniteAndMonotonic(){int old=0;for(int level:new int[]{0,100,255,1000,10000,100000,Integer.MAX_VALUE}){int budget=EnchantmentScaling.throughput(24,level,128);assertTrue(budget>=old&&budget<=128);old=budget;assertTrue(Float.isFinite(EnchantmentScaling.finiteSpeed((double)level*level)));assertTrue(EnchantmentScaling.fortune(level)>=0);}assertEquals(1000,EnchantmentScaling.fortune(1000));}
}
