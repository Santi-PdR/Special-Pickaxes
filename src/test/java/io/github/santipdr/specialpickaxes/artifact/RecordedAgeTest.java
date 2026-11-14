package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecordedAgeTest {
    @Test void epochMillisecondsBecomeWholeTicks(){
        assertEquals(0,RecordedAge.ticks(1049,1000,50));
        assertEquals(1,RecordedAge.ticks(1050,1000,50));
        assertEquals(40,RecordedAge.ticks(3000,1000,50));
    }
    @Test void negativeFutureAndResetClocksNeverCreateFreshMemories(){
        assertEquals(Long.MAX_VALUE,RecordedAge.ticks(100,101,1));
        assertEquals(Long.MAX_VALUE,RecordedAge.ticks(0,100000,1));
        assertEquals(Long.MAX_VALUE,RecordedAge.ticks(100,Long.MIN_VALUE,50));
        assertEquals(Long.MAX_VALUE,RecordedAge.ticks(-1,0,1));
    }
    @Test void extremeValidAgesDoNotWrap(){
        assertEquals(Long.MAX_VALUE,RecordedAge.ticks(Long.MAX_VALUE,0,1));
        assertEquals(1,RecordedAge.ticks(Long.MAX_VALUE,Long.MAX_VALUE-50,50));
        assertEquals(0,RecordedAge.ticks(0,0,1));
    }
    @Test void theClockUnitMustBePositive(){
        assertThrows(IllegalArgumentException.class,()->RecordedAge.ticks(1,0,0));
        assertThrows(IllegalArgumentException.class,()->RecordedAge.ticks(1,0,-1));
    }
}
