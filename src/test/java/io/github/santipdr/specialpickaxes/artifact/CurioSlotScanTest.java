package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CurioSlotScanTest {
    @Test void ordinarySlotCountsAreUnchanged(){
        for(int count=0;count<=64;count++)assertEquals(count,CurioSlotScan.limit(count));
    }
    @Test void malformedNegativeCountsNeverIterate(){
        assertEquals(0,CurioSlotScan.limit(-1));
        assertEquals(0,CurioSlotScan.limit(Integer.MIN_VALUE));
    }
    @Test void oversizedProvidersHaveAFixedInspectionCeiling(){
        for(int count:new int[]{65,1000,Integer.MAX_VALUE})assertEquals(64,CurioSlotScan.limit(count));
    }
    @Test void aCapturedSlotCannotReadOutsideTheCurrentInventory(){
        assertTrue(CurioSlotScan.contains(1,0));
        assertFalse(CurioSlotScan.contains(0,0));
        assertFalse(CurioSlotScan.contains(1,1));
        assertFalse(CurioSlotScan.contains(64,-1));
        assertFalse(CurioSlotScan.contains(64,Integer.MIN_VALUE));
    }
    @Test void anOversizedProviderCannotBypassTheBridgeCeiling(){
        assertTrue(CurioSlotScan.contains(Integer.MAX_VALUE,63));
        assertFalse(CurioSlotScan.contains(Integer.MAX_VALUE,64));
        assertFalse(CurioSlotScan.contains(Integer.MAX_VALUE,Integer.MAX_VALUE));
        assertFalse(CurioSlotScan.contains(Integer.MIN_VALUE,0));
    }
}
