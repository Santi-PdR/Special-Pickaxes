package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class WorldloomBoreGeometryTest {
    @Test void expandedBoreHas2352UniqueCellsInEveryDirection(){
        var origin=new BlockPos(20,80,-40);
        for(var direction:Direction.values()){
            var all=new HashSet<BlockPos>();
            for(int depth=0;depth<48;depth++){
                var center=origin.relative(direction,depth);
                var slice=DirectionalProgram.section(origin,direction,DirectionalProgram.Shape.WORLDLOOM_BORE,depth);
                assertEquals(49,slice.size());
                assertTrue(slice.contains(center));
                for(var cell:slice){
                    assertTrue(all.add(cell));
                    assertEquals(direction.getAxis().choose(center.getX(),center.getY(),center.getZ()),
                            direction.getAxis().choose(cell.getX(),cell.getY(),cell.getZ()));
                    assertTrue(cell.distSqr(center)<=18);
                }
            }
            assertEquals(2352,all.size());
            assertFalse(all.contains(origin.relative(direction,48)));
        }
    }
}
