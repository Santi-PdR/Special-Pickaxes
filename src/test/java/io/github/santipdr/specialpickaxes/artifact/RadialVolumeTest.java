package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class RadialVolumeTest {
    @Test void everyShapeAndRadiusMatchesHistoricalTraversalExactly(){
        for(var shape:RadialVolume.Shape.values())for(int radius=0;radius<=12;radius++){
            var expected=new ArrayList<BlockPos>();
            for(int shell=0;shell<=radius;shell++)for(int x=-shell;x<=shell;x++)
                for(int y=-shell;y<=shell;y++)for(int z=-shell;z<=shell;z++){
                    if(Math.max(Math.max(Math.abs(x),Math.abs(y)),Math.abs(z))!=shell)continue;
                    int distance=x*x+y*y+z*z,inner=Math.max(0,radius-2);
                    boolean inside=switch(shape){
                        case SPHERE -> distance<=radius*radius;
                        case SHELL -> distance<=radius*radius&&distance>=inner*inner;
                        case BLOOM -> HellspecBloomShape.contains(x,y,z);
                        case CUBE -> true;
                    };
                    if(inside)expected.add(new BlockPos(x,y,z));
                }
            var actual=RadialVolume.offsets(shape,radius);
            assertEquals(expected,actual,shape+" radius="+radius);
            assertEquals(actual.size(),new HashSet<>(actual).size());
        }
    }
    @Test void plansAreSharedImmutableAndRadiusClamped(){
        for(var shape:RadialVolume.Shape.values()){
            assertSame(RadialVolume.offsets(shape,12),RadialVolume.offsets(shape,100));
            assertSame(RadialVolume.offsets(shape,0),RadialVolume.offsets(shape,-1));
            assertThrows(UnsupportedOperationException.class,()->RadialVolume.offsets(shape,12).clear());
        }
        assertSame(RadialVolume.offsets(RadialVolume.Shape.CUBE,12).get(0),RadialVolume.offsets(RadialVolume.Shape.SPHERE,12).get(0));
    }
    @Test void knownVolumesAndShellBoundariesRemainStable(){
        assertEquals(7153,RadialVolume.offsets(RadialVolume.Shape.SPHERE,12).size());
        assertEquals(15625,RadialVolume.offsets(RadialVolume.Shape.CUBE,12).size());
        var shell=RadialVolume.offsets(RadialVolume.Shape.SHELL,12);
        assertFalse(shell.contains(new BlockPos(9,0,0)));
        assertTrue(shell.contains(new BlockPos(10,0,0)));
        assertTrue(shell.contains(new BlockPos(12,0,0)));
        assertFalse(shell.contains(new BlockPos(12,1,0)));
    }
}
