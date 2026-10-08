package io.github.santipdr.specialpickaxes.mining;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class BoundedTraversalTest {
    @Test void loopsTerminateAndDeduplicate() {
        var result = BoundedTraversal.walk(0, 100, 100, n -> List.of((n + 1) % 4, n), n -> true);
        assertEquals(List.of(0, 1, 2, 3), result);
    }
    @Test void resultLimitIsInclusiveOfSeed() {
        assertEquals(32, BoundedTraversal.walk(0, 32, 200, n -> List.of(n + 1), n -> true).size());
    }
    @Test void infiniteGraphHonoursVisitBudget() {
        AtomicInteger checks = new AtomicInteger();
        var result = BoundedTraversal.walk(0, 1000, 7, n -> List.of(n + 1, n + 2), n -> { checks.incrementAndGet(); return true; });
        assertEquals(7, result.size());
        assertEquals(7, checks.get());
    }
    @Test void deniedBlocksAreTraversalBarriers() {
        assertEquals(List.of(0, 1), BoundedTraversal.walk(0, 100, 100, n -> List.of(n + 1), n -> n != 2));
    }
    @Test void rejectedSeedDoesNotExpand() {
        assertTrue(BoundedTraversal.walk(0, 10, 10, n -> { fail("must not expand"); return List.of(); }, n -> false).isEmpty());
    }
    @Test void zeroLimitsDoNotInvokeWorld() {
        assertTrue(BoundedTraversal.walk(0, 0, 10, n -> List.of(n + 1), n -> { fail("must not read world"); return true; }).isEmpty());
    }
}
