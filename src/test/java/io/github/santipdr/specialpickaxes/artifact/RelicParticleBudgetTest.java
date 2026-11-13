package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RelicParticleBudgetTest {
    @Test void aBurstReservesAtomicallyAndDenialPreservesRemainder(){
        var budget=new RelicParticleBudget(10);
        assertTrue(budget.tryReserve(20L,6));
        assertFalse(budget.tryReserve(20L,5));
        assertTrue(budget.tryReserve(20L,4));
        assertFalse(budget.tryReserve(20L,1));
    }
    @Test void theFirstTickCanBeAnyLongIncludingTheOldSentinel(){
        var budget=new RelicParticleBudget(10);
        assertTrue(budget.tryReserve(Long.MIN_VALUE,10));
        assertFalse(budget.tryReserve(Long.MIN_VALUE,1));
        assertTrue(budget.tryReserve(Long.MIN_VALUE+1,10));
    }
    @Test void serverTickWrapAndRestartRefreshCapacity(){
        var budget=new RelicParticleBudget(10);
        assertTrue(budget.tryReserve(Integer.MAX_VALUE,10));
        assertTrue(budget.tryReserve(Integer.MIN_VALUE,10));
        assertTrue(budget.tryReserve(0L,10));
        assertFalse(budget.tryReserve(0L,1));
    }
    @Test void zeroCapacityOnlyAcceptsZeroAndInvalidArgumentsAreRejected(){
        var budget=new RelicParticleBudget(0);
        assertTrue(budget.tryReserve(0L,0));
        assertFalse(budget.tryReserve(0L,1));
        assertThrows(IllegalArgumentException.class,()->new RelicParticleBudget(-1));
        assertThrows(IllegalArgumentException.class,()->budget.tryReserve(0L,-1));
    }
}
