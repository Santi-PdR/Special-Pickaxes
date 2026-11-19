package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class WorldloomRootsTest {
    @Test void fourDescendingTerracesHave156UniqueCellsAndLeaveTheScarIntact(){
        var scar=new BlockPos(20,70,-30);var cells=WorldloomRoots.cells(scar);assertEquals(156,cells.size());assertEquals(cells.size(),new HashSet<>(cells).size());assertFalse(cells.contains(scar));
        int previous=0;for(var p:cells){int depth=scar.getY()-p.getY();assertTrue(depth>=1&&depth<=4&&depth>=previous);previous=depth;int r=depth<=2?4:3;int x=p.getX()-scar.getX(),z=p.getZ()-scar.getZ();assertTrue(x*x+z*z<=r*r);}
    }
    @Test void returnedPlanIsImmutableAndDoesNotRetainMutableScar(){
        var scar=new BlockPos.MutableBlockPos(20,70,-30);var cells=WorldloomRoots.cells(scar);scar.set(0,0,0);
        assertTrue(cells.stream().allMatch(p->p.getY()>=66&&p.getY()<=69));assertThrows(UnsupportedOperationException.class,cells::clear);
    }
}
