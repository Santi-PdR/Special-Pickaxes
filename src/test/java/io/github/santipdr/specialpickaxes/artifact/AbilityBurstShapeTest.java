package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class AbilityBurstShapeTest {
    @Test void everyPlayableBurstHasSixteenDistinctFiniteBoundedPoints(){
        assertEquals(16,AbilityBurstShape.COUNT);
        for(var kind:AbilityBurstShape.Shape.values()){
            var points=new HashSet<Vec3>();
            for(int i=0;i<AbilityBurstShape.COUNT;i++){
                var point=AbilityBurstShape.offset(kind,i);
                assertTrue(Double.isFinite(point.lengthSqr()));
                assertTrue(point.lengthSqr()<4);
                assertTrue(points.add(point),kind.name()+" duplicate "+i);
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
        assertThrows(IllegalArgumentException.class,()->AbilityBurstShape.offset(AbilityBurstShape.Shape.ICARUS,-1));
        assertThrows(IllegalArgumentException.class,()->AbilityBurstShape.offset(AbilityBurstShape.Shape.ICARUS,16));
    }
    @Test void secondarySilhouettesAreUniqueBoundedAndNotPrimaryReplays(){
        var signatures=new HashSet<String>();
        for(var kind:AbilityBurstShape.Shape.values()){
            var points=new HashSet<Vec3>();var signature=new StringBuilder();boolean differs=false;
            for(int i=0;i<AbilityBurstShape.COUNT;i++){
                var point=AbilityBurstShape.alternateOffset(kind,i);
                assertTrue(Double.isFinite(point.lengthSqr()));assertTrue(point.lengthSqr()<4);
                assertTrue(points.add(point),kind.name());signature.append(point);
                differs|=point.distanceToSqr(AbilityBurstShape.offset(kind,i))>1e-8;
            }
            assertTrue(differs,kind.name());assertTrue(signatures.add(signature.toString()),kind.name());
        }
    }

    @Test void reusedYawBasisExactlyMatchesOriginalTransformForAllSilhouettes(){
        var looks=new java.util.ArrayList<Vec3>(java.util.List.of(Vec3.ZERO,new Vec3(0,1,0),new Vec3(0,-1,0),
                new Vec3(1e-5,1,0),new Vec3(1e-4,0,0),new Vec3(-1,0,0),new Vec3(0,0,-1)));
        var random=new java.util.Random(749123L);
        for(int i=0;i<100;i++)looks.add(new Vec3(random.nextDouble()*2-1,random.nextDouble()*2-1,random.nextDouble()*2-1));
        for(var look:looks){
            var frame=AbilityBurstShape.yawFrame(look);
            for(var kind:AbilityBurstShape.Shape.values())for(int i=0;i<AbilityBurstShape.COUNT;i++){
                for(var local:new Vec3[]{AbilityBurstShape.offset(kind,i),AbilityBurstShape.alternateOffset(kind,i)}){
                    var forward=new Vec3(look.x,0,look.z);
                    forward=forward.lengthSqr()<1.0E-8?new Vec3(0,0,1):forward.normalize();
                    var right=new Vec3(forward.z,0,-forward.x);
                    var original=right.scale(local.x).add(0,local.y,0).add(forward.scale(local.z));
                    var actual=frame.orient(local);
                    assertEquals(Double.doubleToLongBits(original.x),Double.doubleToLongBits(actual.x));
                    assertEquals(Double.doubleToLongBits(original.y),Double.doubleToLongBits(actual.y));
                    assertEquals(Double.doubleToLongBits(original.z),Double.doubleToLongBits(actual.z));
                    assertEquals(original,AbilityBurstShape.orient(local,look));
                }
            }
        }
    }
}
