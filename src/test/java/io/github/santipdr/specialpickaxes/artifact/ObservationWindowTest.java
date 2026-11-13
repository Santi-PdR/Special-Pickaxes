package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ObservationWindowTest {
    @Test void onlyTheCaptureTickAndTwoFollowingTicksAreFresh(){
        for(int elapsed=0;elapsed<=2;elapsed++)assertTrue(ObservationWindow.fresh(100+elapsed,100));
        assertFalse(ObservationWindow.fresh(103,100));
        assertFalse(ObservationWindow.fresh(99,100));
    }
    @Test void signedServerTickWrapDoesNotExpireAFreshBreak(){
        assertTrue(ObservationWindow.fresh(Integer.MIN_VALUE,Integer.MAX_VALUE));
        assertTrue(ObservationWindow.fresh(Integer.MIN_VALUE+1,Integer.MAX_VALUE));
        assertFalse(ObservationWindow.fresh(Integer.MIN_VALUE+2,Integer.MAX_VALUE));
    }
    @Test void aRestartOrVeryOldObservationIsRejected(){
        assertFalse(ObservationWindow.fresh(0,10000));
        assertFalse(ObservationWindow.fresh(Integer.MAX_VALUE,0));
    }
}
