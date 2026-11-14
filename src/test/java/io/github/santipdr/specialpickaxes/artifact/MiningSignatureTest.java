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
}
