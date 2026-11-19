package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.*;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class EventideRuptureTest {
    @Test void eclipseIsAMassiveFlatDiscNotASphere(){
        var cells=EventideRupture.geometry(BlockPos.ZERO,Direction.SOUTH,EventideRupture.Form.ECLIPSE);
        assertEquals(7063,cells.size());assertEquals(BlockPos.ZERO,cells.get(0));
        assertTrue(cells.contains(new BlockPos(18,3,0)));assertFalse(cells.contains(new BlockPos(0,4,0)));
    }
    @Test void horizonAndAfterglowPreserveTheirCentralCores(){
        for(var form:new EventideRupture.Form[]{EventideRupture.Form.HORIZON,EventideRupture.Form.AFTERGLOW}){
            var cells=EventideRupture.geometry(BlockPos.ZERO,Direction.NORTH,form);assertEquals(form==EventideRupture.Form.HORIZON?5480:312,cells.size());
            int inner=form==EventideRupture.Form.HORIZON?15:2;for(var p:cells)assertTrue(p.getX()*p.getX()+p.getZ()*p.getZ()>=inner*inner);
            assertFalse(cells.contains(BlockPos.ZERO));
        }
    }
    @Test void allFormsAreUniqueImmutableAndVerticalCrescentRotatesWithHeading(){
        for(var form:EventideRupture.Form.values())for(var heading:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){
            var cells=EventideRupture.geometry(BlockPos.ZERO,heading,form);assertEquals(cells.size(),new HashSet<>(cells).size());assertThrows(UnsupportedOperationException.class,cells::clear);
            if(form==EventideRupture.Form.CRESCENT){assertEquals(7063,cells.size());assertTrue(cells.contains(new BlockPos(0,18,0)));assertTrue(cells.contains(BlockPos.ZERO.relative(heading,3)));assertFalse(cells.contains(BlockPos.ZERO.relative(heading,4)));}
        }
    }
}
