package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class RelicParticleStyleTest {
    @Test void stylesCoverEveryBurstShapeWithoutBootstrappingRegistries(){
        var expected=new HashSet<String>();
        for(var kind:AbilityBurstShape.Shape.values())expected.add(kind.name());
        var actual=new HashSet<String>();
        for(var style:RelicParticleStyle.values())actual.add(style.name());
        assertEquals(expected,actual);
    }
    @Test void allMotionsAreBoundedEvenWithInvalidInputs(){
        for(var style:RelicParticleStyle.values()){
            assertTrue(style.lifetime>8 && style.lifetime<40);
            assertTrue(style.size>0 && style.size<.5F);
            for(double t:new double[]{Double.NaN,Double.NEGATIVE_INFINITY,-1,0,.1,.5,.9,1,2,Double.POSITIVE_INFINITY}){
                for(double phase:new double[]{Double.NaN,Double.POSITIVE_INFINITY,0,1,3,6}){
                    var offset=style.displacement(t,phase);
                    assertTrue(Double.isFinite(offset.lengthSqr()),style.name());
                    assertTrue(offset.lengthSqr()<2,style.name());
                    var alternate=style.alternateDisplacement(t,phase);
                    assertTrue(Double.isFinite(alternate.lengthSqr()),style.name());
                    assertTrue(alternate.lengthSqr()<2,style.name());
                }
            }
        }
    }
    @Test void everyRelicHasItsOwnMotionSignature(){
        var signatures=new HashSet<String>();
        for(var style:RelicParticleStyle.values()){
            var signature=new StringBuilder();
            for(double t:new double[]{.2,.5,.8})signature.append(style.displacement(t,.73));
            assertTrue(signatures.add(signature.toString()),style.name());
        }
    }
    @Test void secondariesHaveDistinctMotionFromTheirPrimaryAndFromOneAnother(){
        var signatures=new HashSet<String>();
        for(var style:RelicParticleStyle.values()){
            var signature=new StringBuilder();
            boolean differs=false;
            for(double t:new double[]{.2,.5,.8}){
                var point=style.alternateDisplacement(t,.73);
                signature.append(point);
                differs|=point.distanceToSqr(style.displacement(t,.73))>1e-8;
            }
            assertTrue(differs,style.name());
            assertTrue(signatures.add(signature.toString()),style.name());
        }
    }

}
