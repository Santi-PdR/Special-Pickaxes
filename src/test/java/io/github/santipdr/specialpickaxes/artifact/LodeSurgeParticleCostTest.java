package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LodeSurgeParticleCostTest {
    @Test void oreCostsIncludeTheSinglePeakAccent(){
        assertEquals(3,LodeSurgeEffect.orePulseCost(0));
        assertEquals(4,LodeSurgeEffect.orePulseCost(1));
        assertEquals(5,LodeSurgeEffect.orePulseCost(2));
        assertEquals(7,LodeSurgeEffect.orePulseCost(3));
    }
    @Test void sharedCostsCountEachViewerAndTheRecipientOnlyAccent(){
        assertEquals(8,LodeSurgeEffect.sharedPulseCost(1));
        assertEquals(8,LodeSurgeEffect.sharedPulseCost(2));
        assertEquals(9,LodeSurgeEffect.sharedPulseCost(3));
    }
    @Test void malformedStackCountsStayBounded(){
        assertEquals(3,LodeSurgeEffect.orePulseCost(Integer.MIN_VALUE));
        assertEquals(7,LodeSurgeEffect.orePulseCost(Integer.MAX_VALUE));
        assertEquals(8,LodeSurgeEffect.sharedPulseCost(Integer.MIN_VALUE));
        assertEquals(9,LodeSurgeEffect.sharedPulseCost(Integer.MAX_VALUE));
    }
}
