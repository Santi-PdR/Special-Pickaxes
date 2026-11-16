package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class ResonantGalleryGeometryTest {
    @Test void galleryHasTwentyFiveUniqueCellsPerSliceInEveryDirection(){
        for(var direction:Direction.values()){
            var all=new HashSet<BlockPos>();
            for(int depth=0;depth<48;depth++){
                var cells=DirectionalProgram.section(BlockPos.ZERO,direction,DirectionalProgram.Shape.RESONANT_TUNNEL,depth);
                assertEquals(25,cells.size());
                var center=BlockPos.ZERO.relative(direction,depth);
                assertTrue(cells.contains(center));
                for(var cell:cells){
                    assertTrue(all.add(cell));
                    assertEquals(direction.getAxis().choose(center.getX(),center.getY(),center.getZ()),
                            direction.getAxis().choose(cell.getX(),cell.getY(),cell.getZ()));
                    assertTrue(cell.distSqr(center)<=8);
                }
            }
            assertEquals(1200,all.size());
        }
    }
}
