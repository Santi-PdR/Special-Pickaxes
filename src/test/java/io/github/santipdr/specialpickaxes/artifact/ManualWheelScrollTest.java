package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManualWheelScrollTest {
    @Test void fractionalTrackpadDeltasAccumulateInsteadOfJumpingWholeLines(){
        var wheel=new ManualWheelScroll();int position=0;
        for(int i=0;i<8;i++)position=wheel.move(position,100,-.125,4);
        assertEquals(4,position);
        assertEquals(32,wheel.move(position,100,-1,28));
    }
    @Test void outwardPressureAtEdgesDoesNotDelayTheNextInwardMovement(){
        var wheel=new ManualWheelScroll();
        assertEquals(0,wheel.move(0,100,.125,4));
        assertEquals(1,wheel.move(0,100,-.25,4));
        assertEquals(100,wheel.move(100,100,-.125,4));
        assertEquals(99,wheel.move(100,100,.25,4));
    }
    @Test void reversalDiscardsOldFractionAndResetSeparatesNewLayouts(){
        var wheel=new ManualWheelScroll();
        assertEquals(50,wheel.move(50,100,-.125,4));
        assertEquals(49,wheel.move(50,100,.25,4));
        wheel.move(50,100,-.125,4);wheel.reset();
        assertEquals(50,wheel.move(50,100,-.125,4));
    }
    @Test void invalidAndExtremeDeltasCannotOverflowOrPoisonLaterInput(){
        var wheel=new ManualWheelScroll();
        for(double delta:new double[]{Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY})
            assertEquals(50,wheel.move(50,100,delta,28));
        assertEquals(Integer.MAX_VALUE,wheel.move(10,Integer.MAX_VALUE,-Double.MAX_VALUE,28));
        assertEquals(0,wheel.move(10,Integer.MAX_VALUE,Double.MAX_VALUE,28));
        assertEquals(78,wheel.move(50,100,-1,28));
        assertEquals(0,wheel.move(-100,-1,-1,28));
    }
    @Test void contentAndPickerAccumulatorsAreIndependent(){
        var content=new ManualWheelScroll();var picker=new ManualWheelScroll();
        assertEquals(0,content.move(0,100,-.125,4));
        assertEquals(0,picker.move(0,100,-.125,4));
        assertEquals(1,content.move(0,100,-.125,4));
    }
}
