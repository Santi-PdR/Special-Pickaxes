package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class MiningSignatureTest {
    @Test void everyPlayablePickHasItsOwnBoundedGeometry(){
        var shapes=new HashSet<Object>();
        for(int kind=0;kind<13;kind++){
            var points=MiningSignature.points(kind,Vec3.ZERO,.4);
            assertEquals(16,points.size());assertTrue(shapes.add(points),"style "+kind);
            for(var p:points)assertTrue(Double.isFinite(p.x)&&Double.isFinite(p.y)&&Double.isFinite(p.z)&&p.lengthSqr()<4);
        }
        assertEquals(13,shapes.size());
    }
    @Test void signaturesTranslateWithTheToolBearer(){
        var origin=new Vec3(12,64,-8);
        for(int kind=0;kind<13;kind++){
            var base=MiningSignature.points(kind,Vec3.ZERO,0);var moved=MiningSignature.points(kind,origin,0);
            for(int i=0;i<base.size();i++)assertTrue(moved.get(i).distanceTo(base.get(i).add(origin))<1e-8);
        }
    }
    @Test void orientationPreservesScaleAndVerticalShape(){
        var local=new Vec3(.3,.4,.6);
        for(var look:new Vec3[]{new Vec3(1,0,0),new Vec3(0,0,-1),new Vec3(0,1,0),Vec3.ZERO}){
            var transformed=MiningSignature.orient(local,look);
            assertEquals(local.y,transformed.y,1e-9);assertEquals(local.lengthSqr(),transformed.lengthSqr(),1e-9);
        }
        assertEquals(.6,MiningSignature.orient(local,new Vec3(1,0,0)).x,1e-9);
    }
    @Test void malformedLookFallsBackToAFiniteHorizontalBasis(){
        var result=MiningSignature.orient(new Vec3(.2,.3,.4),new Vec3(Double.NaN,0,Double.POSITIVE_INFINITY));
        assertTrue(Double.isFinite(result.x)&&Double.isFinite(result.y)&&Double.isFinite(result.z));
    }
    @Test void malformedPhaseNeverCreatesInvalidParticles(){
        for(int style=0;style<13;style++)for(var p:MiningSignature.points(style,Vec3.ZERO,Double.NaN))
            assertTrue(Double.isFinite(p.x)&&Double.isFinite(p.y)&&Double.isFinite(p.z));
    }
}
