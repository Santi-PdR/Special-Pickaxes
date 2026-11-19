package io.github.santipdr.specialpickaxes.artifact;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class StasisLifetimeTest {
    @Test void lifetimeShrinksBetweenServerUpdates(){
        assertEquals(1,StasisLifetime.fraction(600,600,0));
        assertEquals(.5,StasisLifetime.fraction(320,600,20));
        assertEquals(0,StasisLifetime.fraction(10,600,11));
    }
    @Test void malformedAndOverflowingTimesStayBounded(){
        assertEquals(0,StasisLifetime.fraction(600,0,0));
        assertEquals(1,StasisLifetime.fraction(700,600,-1));
        assertEquals(0,StasisLifetime.fraction(600,600,Long.MAX_VALUE));
        assertEquals(0,StasisLifetime.fraction(-1,600,0));
    }
}
