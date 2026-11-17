package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class HellspecBloomShapeTest {
    @Test void expandedBloomHasExactly1679UniqueSymmetricCells(){
        var cells=new HashSet<BlockPos>();
        for(int x=-13;x<=13;x++)for(int y=-4;y<=4;y++)for(int z=-13;z<=13;z++){
            boolean inside=HellspecBloomShape.contains(x,y,z);
            assertEquals(inside,HellspecBloomShape.contains(-x,-y,-z));
            assertEquals(inside,HellspecBloomShape.contains(z,y,x));
            if(inside){assertTrue(Math.abs(x)<=12&&Math.abs(z)<=12&&Math.abs(y)<=3);cells.add(new BlockPos(x,y,z));}
        }
        assertEquals(1679,cells.size());
        assertEquals(cells,new HashSet<>(RadialVolume.offsets(RadialVolume.Shape.BLOOM,HellspecBloomShape.RADIUS)));
        assertTrue(cells.contains(new BlockPos(12,0,0)));
        assertTrue(cells.contains(new BlockPos(0,3,0)));
        assertFalse(cells.contains(new BlockPos(12,1,0)));
    }
    @Test void cachedBoundaryIsExactlyTheSixFaceSurface(){
        var boundary=HellspecBloomShape.boundary();
        assertSame(boundary,HellspecBloomShape.boundary());
        assertEquals(boundary.size(),new HashSet<>(boundary).size());
        var expected=new HashSet<BlockPos>();
        for(var pos:RadialVolume.offsets(RadialVolume.Shape.BLOOM,12)){
            for(var direction:Direction.values()){
                var neighbor=pos.relative(direction);
                if(!HellspecBloomShape.contains(neighbor.getX(),neighbor.getY(),neighbor.getZ())){expected.add(pos);break;}
            }
        }
        assertEquals(expected,new HashSet<>(boundary));
        assertThrows(UnsupportedOperationException.class,()->boundary.add(BlockPos.ZERO));
    }
}
