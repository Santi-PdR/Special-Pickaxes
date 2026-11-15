package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IcarianShaftTest {
    @Test void theShaftTracksItsOwnProgressRatherThanTheOriginalFloor(){
        var base=new BlockPos(10,64,-8);
        for(int rise=0;rise<384;rise++)assertTrue(IcarianShaft.contains(base,rise,new Vec3(10.5,64+rise+.05,-7.5)));
    }
    @Test void leavingAnyHorizontalEdgeStopsTheLift(){
        var base=new BlockPos(10,64,-8);
        assertTrue(IcarianShaft.contains(base,0,new Vec3(10,64,-8)));
        for(var feet:new Vec3[]{new Vec3(9.999,64,-7.5),new Vec3(11,64,-7.5),new Vec3(10.5,64,-8.001),new Vec3(10.5,64,-7)})
            assertFalse(IcarianShaft.contains(base,0,feet));
    }
    @Test void normalJumpToleranceDoesNotPermitRemoteVerticalRecapture(){
        var base=new BlockPos(0,64,0);
        assertTrue(IcarianShaft.contains(base,4,new Vec3(.5,70.5,.5)));
        assertFalse(IcarianShaft.contains(base,4,new Vec3(.5,70.501,.5)));
        assertFalse(IcarianShaft.contains(base,0,new Vec3(.5,60,.5)));
    }
    @Test void malformedCoordinatesNeverCreateAnAnchor(){
        for(double value:new double[]{Double.NaN,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY}){
            assertFalse(IcarianShaft.contains(BlockPos.ZERO,0,new Vec3(value,0,0)));
            assertFalse(IcarianShaft.contains(BlockPos.ZERO,0,new Vec3(0,value,0)));
            assertFalse(IcarianShaft.contains(BlockPos.ZERO,0,new Vec3(0,0,value)));
        }
        assertFalse(IcarianShaft.contains(BlockPos.ZERO,-1,Vec3.ZERO));
    }
    @Test void integerExtremesCannotWrapTheExpectedAltitude(){
        var base=new BlockPos(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE);
        assertTrue(IcarianShaft.contains(base,384,new Vec3((double)Integer.MAX_VALUE+.5,(double)Integer.MAX_VALUE+384,(double)Integer.MIN_VALUE+.5)));
    }
}
