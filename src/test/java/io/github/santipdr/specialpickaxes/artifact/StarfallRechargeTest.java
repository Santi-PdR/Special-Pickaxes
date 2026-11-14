package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StarfallRechargeTest {
    @Test void ordinaryRechargeKeepsItsThirtyTickProgression(){
        assertEquals(0,StarfallWardEffect.chargeProgress(100,130),1e-9);
        assertEquals(.5,StarfallWardEffect.chargeProgress(115,130),1e-9);
        assertEquals(1,StarfallWardEffect.chargeProgress(130,130),1e-9);
        assertFalse(StarfallWardEffect.shouldPredictiveScan(0,0,100,130));
    }
    @Test void impossibleDeadlinesRestoreReadyGeometryAndPrediction(){
        assertEquals(1,StarfallWardEffect.chargeProgress(100,Long.MAX_VALUE),1e-9);
        assertEquals(1,StarfallWardEffect.chargeProgress(Long.MIN_VALUE,Long.MAX_VALUE),1e-9);
        assertTrue(StarfallWardEffect.shouldPredictiveScan(0,0,100,Long.MAX_VALUE));
        assertFalse(StarfallWardEffect.shouldPredictiveScan(0,1,100,Long.MAX_VALUE));
    }
    @Test void readyCadenceRemainsStaggeredAcrossTheSignedTickWrap(){
        assertTrue(StarfallWardEffect.shouldPredictiveScan(Integer.MAX_VALUE,1,130,130));
        assertFalse(StarfallWardEffect.shouldPredictiveScan(Integer.MIN_VALUE,1,130,130));
        assertTrue(StarfallWardEffect.shouldPredictiveScan(Integer.MIN_VALUE,0,130,130));
    }
}
