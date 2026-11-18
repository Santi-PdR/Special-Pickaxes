package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManualChoiceNavigationTest {
    @Test void focusStaysInsideThePickerAndDoesNotWrap(){
        assertEquals(-1,ManualChoiceNavigation.step(0,1,0));
        assertEquals(0,ManualChoiceNavigation.step(0,-1,13));
        assertEquals(12,ManualChoiceNavigation.step(12,1,13));
        assertEquals(6,ManualChoiceNavigation.step(5,1,13));
        assertEquals(4,ManualChoiceNavigation.step(5,-1,13));
        assertEquals(12,ManualChoiceNavigation.step(Integer.MAX_VALUE,1,13));
    }
    @Test void focusIsRevealedWithoutMovingAnAlreadyVisibleCell(){
        assertEquals(0,ManualChoiceNavigation.reveal(0,2,13,24,100));
        assertEquals(20,ManualChoiceNavigation.reveal(0,4,13,24,100));
        assertEquals(48,ManualChoiceNavigation.reveal(100,2,13,24,100));
        assertEquals(212,ManualChoiceNavigation.reveal(0,12,13,24,100));
        assertEquals(0,ManualChoiceNavigation.reveal(100,12,13,24,600));
    }
    @Test void everyNormalViewportFullyContainsItsFocusedCell(){
        for(int viewport=24;viewport<=760;viewport++)for(int index=0;index<13;index++)for(int previous:new int[]{-1,0,100,Integer.MAX_VALUE}){
            int scroll=ManualChoiceNavigation.reveal(previous,index,13,24,viewport);
            assertTrue(scroll>=0&&scroll<=Math.max(0,13*24-viewport));
            assertTrue(index*24>=scroll&&(index+1)*24<=scroll+viewport);
        }
    }
    @Test void invalidAndExtremeInputsCannotOverflow(){
        assertEquals(0,ManualChoiceNavigation.reveal(0,-1,13,24,100));
        assertEquals(0,ManualChoiceNavigation.reveal(0,13,13,24,100));
        assertEquals(0,ManualChoiceNavigation.reveal(0,1,13,0,100));
        assertTrue(ManualChoiceNavigation.reveal(Integer.MAX_VALUE,Integer.MAX_VALUE-1,Integer.MAX_VALUE,Integer.MAX_VALUE,1)>=0);
    }
}
