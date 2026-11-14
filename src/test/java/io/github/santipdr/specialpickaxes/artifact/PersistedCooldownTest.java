package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PersistedCooldownTest {
    @Test void theWholeIntendedIntervalIsPreserved(){
        for(int elapsed=0;elapsed<40;elapsed++)assertTrue(PersistedCooldown.active(100+elapsed,140,40));
        assertFalse(PersistedCooldown.active(140,140,40));
        assertFalse(PersistedCooldown.active(141,140,40));
    }
    @Test void futureDeadlinesOutsideTheIntervalAreStale(){
        assertFalse(PersistedCooldown.active(99,140,40));
        assertFalse(PersistedCooldown.active(0,100000,100));
        assertFalse(PersistedCooldown.active(100,Long.MAX_VALUE,160));
    }
    @Test void longExtremesCannotWrapIntoAnActiveCooldown(){
        assertFalse(PersistedCooldown.active(Long.MIN_VALUE,Long.MAX_VALUE,160));
        assertFalse(PersistedCooldown.active(Long.MAX_VALUE,Long.MIN_VALUE,160));
        assertTrue(PersistedCooldown.active(Long.MIN_VALUE,Long.MIN_VALUE+20,20));
        assertTrue(PersistedCooldown.active(Long.MAX_VALUE-20,Long.MAX_VALUE,20));
    }
    @Test void zeroNeverBlocksAndNegativeIntervalsAreRejected(){
        assertFalse(PersistedCooldown.active(100,101,0));
        assertThrows(IllegalArgumentException.class,()->PersistedCooldown.active(100,101,-1));
    }
}
