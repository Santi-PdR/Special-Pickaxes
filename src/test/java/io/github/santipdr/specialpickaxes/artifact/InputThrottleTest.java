package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InputThrottleTest {
    @Test void ordinaryRepeatSuppressionIncludesTheOriginalTick(){
        assertTrue(InputThrottle.recent(100,100,2));
        assertTrue(InputThrottle.recent(101,100,2));
        assertFalse(InputThrottle.recent(102,100,2));
    }
    @Test void rollbackOrAnImpossibleFutureNeverDisablesControls(){
        assertFalse(InputThrottle.recent(99,100,2));
        assertFalse(InputThrottle.recent(0,Long.MAX_VALUE,2));
    }
    @Test void subtractionOverflowCannotTurnAncientInputIntoARecentOne(){
        assertFalse(InputThrottle.recent(100,Long.MIN_VALUE,2));
        assertFalse(InputThrottle.recent(Long.MAX_VALUE,Long.MIN_VALUE,2));
        assertFalse(InputThrottle.recent(Long.MIN_VALUE,Long.MAX_VALUE,2));
    }
    @Test void extremeValidTimesStillRespectTheExactInterval(){
        assertTrue(InputThrottle.recent(Long.MIN_VALUE+1,Long.MIN_VALUE,2));
        assertTrue(InputThrottle.recent(Long.MAX_VALUE,Long.MAX_VALUE-1,2));
        assertFalse(InputThrottle.recent(Long.MAX_VALUE,Long.MAX_VALUE-2,2));
    }
    @Test void zeroDisablesThrottlingAndNegativeIntervalsAreRejected(){
        assertFalse(InputThrottle.recent(100,100,0));
        assertThrows(IllegalArgumentException.class,()->InputThrottle.recent(100,100,-1));
    }
}
