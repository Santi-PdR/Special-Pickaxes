package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class OverhaulGeometryTest {
    @Test void orefallIsUniqueBoundedAndNearestFirst(){
        var offsets=OrefallProgram.scanOffsets();
        assertEquals(BlockPos.ZERO,offsets.get(0));
        assertEquals(offsets.size(),new HashSet<>(offsets).size());
        assertTrue(offsets.size()>25000&&offsets.size()<30000);
        double previous=-1;
        for(var pos:offsets){
            assertTrue(pos.getX()*pos.getX()+pos.getZ()*pos.getZ()+4*pos.getY()*pos.getY()<=576);
            double distance=pos.distSqr(BlockPos.ZERO);
            assertTrue(distance>=previous);previous=distance;
        }
        assertTrue(offsets.contains(new BlockPos(24,0,0)));
        assertTrue(offsets.contains(new BlockPos(0,12,0)));
        assertFalse(offsets.contains(new BlockPos(0,13,0)));
        assertThrows(UnsupportedOperationException.class,()->offsets.add(BlockPos.ZERO));
    }
    @Test void icarusCutsHaveExactAreaAndOrientationInAllSixDirections(){
        var origin=new BlockPos(10,70,-30);
        for(var direction:Direction.values()){
            for(var shape:new DirectionalProgram.Shape[]{DirectionalProgram.Shape.ICARUS,DirectionalProgram.Shape.ICARUS_WIDE}){
                var cells=DirectionalProgram.section(origin,direction,shape,8);
                assertEquals(shape==DirectionalProgram.Shape.ICARUS?77:135,cells.size());
                assertEquals(cells.size(),new HashSet<>(cells).size());
                var center=origin.relative(direction,8);
                assertTrue(cells.contains(center));
                for(var pos:cells)assertEquals(direction.getAxis().choose(center.getX(),center.getY(),center.getZ()),
                        direction.getAxis().choose(pos.getX(),pos.getY(),pos.getZ()));
            }
        }
    }
}
