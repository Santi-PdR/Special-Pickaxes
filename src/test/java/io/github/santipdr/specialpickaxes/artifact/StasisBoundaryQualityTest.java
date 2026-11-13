package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StasisBoundaryQualityTest {
    @Test void particleQualityProgressivelyReducesSparkFrequency(){
        assertEquals(4,StasisBoundaryGeometry.particleInterval(0));
        assertEquals(8,StasisBoundaryGeometry.particleInterval(1));
        assertEquals(16,StasisBoundaryGeometry.particleInterval(2));
        for(int quality=0;quality<3;quality++){
            int pulses=0;
            for(int tick=0;tick<64;tick++)if(StasisBoundaryGeometry.particleDue(tick,quality))pulses++;
            assertEquals(16>>quality,pulses);
        }
    }
    @Test void invalidQualityIsClampedWithoutZeroIntervals(){
        assertEquals(4,StasisBoundaryGeometry.particleInterval(-1));
        assertEquals(16,StasisBoundaryGeometry.particleInterval(Integer.MAX_VALUE));
    }
    @Test void longClientSessionsDoNotTruncateTheClock(){
        assertTrue(StasisBoundaryGeometry.particleDue(1L<<40,2));
        assertFalse(StasisBoundaryGeometry.particleDue((1L<<40)+1L,2));
        assertTrue(StasisBoundaryGeometry.particleDue(-16L,2));
    }
}
