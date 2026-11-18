package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class RelicParticleStyleTest {
    @Test void stylesCoverExactlyThePlayableRoster(){
        var expected=new HashSet<String>();
        for(var kind:ArtifactKind.values())if(kind.playable())expected.add(kind.name());
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
}
