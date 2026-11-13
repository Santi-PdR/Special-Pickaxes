package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProjectileScanBudgetTest {
    @Test void onlyIndependentTypesSplitTheAllowance(){
        assertEquals(new ProjectileScanBudget(1024,0),ProjectileScanBudget.split(1024,false));
        assertEquals(new ProjectileScanBudget(512,512),ProjectileScanBudget.split(1024,true));
    }
    @Test void oddTinyAndExtremeAllowancesNeverExceedTotal(){
        for(int total:new int[]{0,1,2,3,17,1023,Integer.MAX_VALUE}){
            var split=ProjectileScanBudget.split(total,true);
            assertTrue(split.vanilla()>=0&&split.optional()>=0);
            assertEquals((long)total,(long)split.vanilla()+split.optional());
            assertTrue(split.vanilla()>=split.optional());
        }
        assertEquals(new ProjectileScanBudget(1,0),ProjectileScanBudget.split(1,true));
    }
    @Test void invalidAllowancesDoNotAuthorizeQueries(){
        assertEquals(new ProjectileScanBudget(0,0),ProjectileScanBudget.split(-1,true));
        assertEquals(new ProjectileScanBudget(0,0),ProjectileScanBudget.split(Integer.MIN_VALUE,false));
    }
}
