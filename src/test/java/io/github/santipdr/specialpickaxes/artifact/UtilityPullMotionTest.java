package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UtilityPullMotionTest {
    @Test void eventideKeepsItsFixedPullSpeedAndDirection(){
        var v=UtilityPullMotion.velocity(new Vec3(3,0,4),new Vec3(50,0,0),0,.65,.65);
        assertEquals(.65,v.length(),1e-12);assertEquals(.39,v.x,1e-12);assertEquals(.52,v.z,1e-12);
    }
    @Test void inheritedVelocityCannotExceedTheRecallCap(){
        var v=UtilityPullMotion.velocity(new Vec3(0,0,10),new Vec3(1e200,1e200,1e200),.2,.85,1.05);
        assertTrue(v.length()<=1.05+1e-12);assertTrue(v.z>0);assertTrue(Double.isFinite(v.x));
    }
    @Test void malformedInheritedVelocityRecoversTowardTheMiner(){
        for(double bad:new double[]{Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY}){
            var v=UtilityPullMotion.velocity(new Vec3(0,4,0),new Vec3(bad,0,0),.55,.28,.6);
            assertEquals(0,v.x,1e-12);assertEquals(.28,v.y,1e-12);assertEquals(0,v.z,1e-12);
        }
    }
    @Test void noDirectionOrMalformedTargetStopsSafely(){
        assertEquals(Vec3.ZERO,UtilityPullMotion.velocity(Vec3.ZERO,Vec3.ZERO,.2,.85,1.05));
        assertEquals(Vec3.ZERO,UtilityPullMotion.velocity(new Vec3(Double.NaN,0,0),Vec3.ZERO,.2,.85,1.05));
    }
    @Test void largeFiniteCoordinatesKeepAUsableDirection(){
        var v=UtilityPullMotion.velocity(new Vec3(1e200,0,0),Vec3.ZERO,0,.65,.65);
        assertEquals(.65,v.x,1e-12);
    }
    @Test void invalidProfilesCannotProduceNanMotion(){
        for(double invalid:new double[]{-1,Double.NaN,Double.POSITIVE_INFINITY}){
            assertThrows(IllegalArgumentException.class,()->UtilityPullMotion.velocity(Vec3.ZERO,Vec3.ZERO,invalid,.5,1));
            assertThrows(IllegalArgumentException.class,()->UtilityPullMotion.velocity(Vec3.ZERO,Vec3.ZERO,.5,invalid,1));
            assertThrows(IllegalArgumentException.class,()->UtilityPullMotion.velocity(Vec3.ZERO,Vec3.ZERO,.5,.5,invalid));
        }
    }
}
