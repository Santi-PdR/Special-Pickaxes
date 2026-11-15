package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MiningPulseCostTest {
    @Test void ordinarySignaturesMatchTheirEmittedMotes(){
        assertEquals(4,MiningPulseCost.perViewer(3,1));
        assertEquals(5,MiningPulseCost.perViewer(5,2));
        assertEquals(6,MiningPulseCost.perViewer(3,3));
    }
    @Test void invalidCountsCannotOverflowOrCreateNegativeReservations(){
        assertEquals(0,MiningPulseCost.perViewer(Integer.MIN_VALUE,-1));
        assertEquals(6,MiningPulseCost.perViewer(Integer.MAX_VALUE,Integer.MAX_VALUE));
        assertEquals(3,MiningPulseCost.perViewer(-1,Integer.MAX_VALUE));
    }
    @Test void visibleCountHasExactlyTheSameThreeMoteCeilingForBothChannels(){
        for(int requested=-10;requested<=100;requested++){
            int count=MiningPulseCost.visible(requested);
            assertTrue(count>=0&&count<=3);
            assertEquals(count*2,MiningPulseCost.perViewer(requested,requested));
        }
    }
}
