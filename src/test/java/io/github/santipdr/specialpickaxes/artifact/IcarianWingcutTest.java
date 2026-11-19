package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class IcarianWingcutTest {
    @Test void bothModesAreUniqueMirroredBoundedWingsInEveryDirection(){
        for(var forward:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST})for(boolean wide:new boolean[]{false,true}){
            var cells=IcarianWingcut.geometry(BlockPos.ZERO,forward,wide);var set=new HashSet<>(cells);
            assertEquals(wide?5740:3820,cells.size());assertEquals(cells.size(),set.size());
            assertTrue(cells.size()<8192);assertTrue(set.contains(BlockPos.ZERO));
            for(var cell:cells){
                int depth=cell.getX()*forward.getStepX()+cell.getZ()*forward.getStepZ();
                var center=BlockPos.ZERO.relative(forward,depth);var mirrored=new BlockPos(2*center.getX()-cell.getX(),cell.getY(),2*center.getZ()-cell.getZ());
                assertTrue(set.contains(mirrored));assertTrue(depth>=0&&depth<32);
                assertTrue(cell.getY()>=-2&&cell.getY()<=14);
            }
        }
    }
    @Test void verticalDirectionsAreRejected(){assertThrows(IllegalArgumentException.class,()->IcarianWingcut.geometry(BlockPos.ZERO,Direction.UP,false));}
    @Test void thrustRampsSmoothlyWithoutUnboundedVelocity(){
        double previous=0;
        for(int age=0;age<=100;age++){
            double speed=IcarianFlight.speed(age);assertTrue(speed>=previous&&speed<=.70);assertTrue(age==0||speed-previous<=.0350001);previous=speed;
        }
        assertEquals(.16,IcarianFlight.speed(0),.0001);assertEquals(.70,IcarianFlight.speed(31),.0001);
    }
}
