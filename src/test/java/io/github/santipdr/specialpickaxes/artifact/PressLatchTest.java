package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PressLatchTest {
    @Test void holdingAcrossGuiCloseNeverBecomesAnotherPress(){
        var latch=new PressLatch();
        assertTrue(latch.update(true));
        for(int tick=0;tick<200;tick++)assertFalse(latch.update(true));
        assertFalse(latch.update(false));
        assertTrue(latch.update(true));
    }
    @Test void primingOnConnectionChangeRequiresReleaseBeforeANewEdge(){
        var latch=new PressLatch();
        latch.update(true); // The reconnect branch deliberately ignores this edge.
        assertFalse(latch.update(true));
        assertFalse(latch.update(false));
        assertTrue(latch.update(true));
    }
}
