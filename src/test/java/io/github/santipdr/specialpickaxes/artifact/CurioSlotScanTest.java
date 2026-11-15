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
}
