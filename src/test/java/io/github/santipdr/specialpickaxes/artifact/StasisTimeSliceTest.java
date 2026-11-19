package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class StasisTimeSliceTest {
    @Test void sliceHas112UniqueHorizontalCellsAndExcludesTheScar(){
        var scar=new BlockPos(20,70,-30);var cells=StasisTimeSlice.cells(scar);
        assertEquals(112,cells.size());assertEquals(112,new HashSet<>(cells).size());assertFalse(cells.contains(scar));
        for(var p:cells){assertEquals(70,p.getY());assertTrue(p.distSqr(scar)<=36);}
        assertThrows(UnsupportedOperationException.class,cells::clear);
    }
    @Test void mutableScarDoesNotMoveTheStoredSlice(){
        var scar=new BlockPos.MutableBlockPos(20,70,-30);var cells=StasisTimeSlice.cells(scar);scar.set(0,0,0);
        assertTrue(cells.stream().allMatch(p->p.getY()==70));
    }
}
