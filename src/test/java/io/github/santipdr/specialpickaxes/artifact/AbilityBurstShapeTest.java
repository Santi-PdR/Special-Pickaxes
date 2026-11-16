package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class AbilityBurstShapeTest {
    @Test void everyPlayableBurstHasSixteenDistinctFiniteBoundedPoints(){
        assertEquals(16,AbilityBurstShape.COUNT);
        for(var kind:ArtifactKind.playableValues()){
            var points=new HashSet<Vec3>();
            for(int i=0;i<AbilityBurstShape.COUNT;i++){
                var point=AbilityBurstShape.offset(kind,i);
                assertTrue(Double.isFinite(point.lengthSqr()));
                assertTrue(point.lengthSqr()<4);
                assertTrue(points.add(point),kind.id+" duplicate "+i);
            }
        }
    }
    @Test void straightUpDownAndZeroLookHaveStableYawFrames(){
        var local=new Vec3(.3,.4,.5);
        for(var look:new Vec3[]{Vec3.ZERO,new Vec3(0,1,0),new Vec3(0,-1,0),new Vec3(1,0,0),new Vec3(1,2,3)}){
            var world=AbilityBurstShape.orient(local,look);
            assertEquals(local.lengthSqr(),world.lengthSqr(),1.0E-10);
            assertEquals(local.y,world.y,1.0E-10);
        }
    }
    @Test void invalidIndicesAreRejected(){
        assertThrows(IllegalArgumentException.class,()->AbilityBurstShape.offset(ArtifactKind.ICARUS,-1));
        assertThrows(IllegalArgumentException.class,()->AbilityBurstShape.offset(ArtifactKind.ICARUS,16));
    }
}
