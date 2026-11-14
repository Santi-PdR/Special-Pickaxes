package io.github.santipdr.specialpickaxes.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParticleEnvelopeTest {
    @Test void birthAndDeathAreTransparent(){
        assertEquals(0F,ParticleEnvelope.alpha(0,16,2,4));
        assertEquals(0F,ParticleEnvelope.alpha(16,16,2,4));
        assertEquals(0F,ParticleEnvelope.alpha(17,16,2,4));
        assertEquals(1F,ParticleEnvelope.alpha(5,16,2,4));
    }
    @Test void opacityStaysFiniteAndBoundedAcrossEveryAuthoredLifetime(){
        for(int lifetime:new int[]{10,12,16,18,20})for(float age=-1;age<=lifetime+1;age+=.125F){
            float alpha=ParticleEnvelope.alpha(age,lifetime,2,4);
            assertTrue(Float.isFinite(alpha)&&alpha>=0&&alpha<=1);
        }
    }
    @Test void endpointsEaseAndTheFadeIsMonotonic(){
        float previous=0;
        for(float age=0;age<=2;age+=.125F){float next=ParticleEnvelope.alpha(age,16,2,4);assertTrue(next>=previous);previous=next;}
        previous=1;
        for(float age=12;age<=16;age+=.125F){float next=ParticleEnvelope.alpha(age,16,2,4);assertTrue(next<=previous);previous=next;}
        assertTrue(ParticleEnvelope.alpha(.25F,16,2,4)<.125F);
    }
    @Test void invalidInputsAndOverlappingFadesStaySafe(){
        assertEquals(0F,ParticleEnvelope.alpha(Float.NaN,16,2,4));
        assertEquals(0F,ParticleEnvelope.alpha(Float.POSITIVE_INFINITY,16,2,4));
        assertEquals(0F,ParticleEnvelope.alpha(1,0,2,4));
        assertEquals(1F,ParticleEnvelope.alpha(1,2,0,0));
        assertTrue(ParticleEnvelope.alpha(1,2,10,10)<1);
    }
}
