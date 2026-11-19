package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class HellspecInfernalRiftsTest {
    @Test void eightFissuresHave4049UniqueCellsInTopDownOrder(){
        assertEquals(4049,HellspecInfernalRifts.VOLUME);var seen=new HashSet<BlockPos>();int previous=4;
        for(int i=0;i<HellspecInfernalRifts.VOLUME;i++){
            var p=HellspecInfernalRifts.cell(BlockPos.ZERO,i);assertTrue(seen.add(p));assertTrue(p.getY()<=previous);previous=p.getY();
            assertTrue(HellspecInfernalRifts.contains(p.getX(),p.getY(),p.getZ()));
        }
    }
    @Test void riftsNarrowAtDepthAndLeaveRockBetweenTheirEightArms(){
        assertTrue(HellspecInfernalRifts.contains(18,4,0));assertFalse(HellspecInfernalRifts.contains(18,-8,0));
        assertTrue(HellspecInfernalRifts.contains(9,-8,9));assertFalse(HellspecInfernalRifts.contains(8,0,4));
        assertTrue(HellspecInfernalRifts.contains(0,-8,0));assertFalse(HellspecInfernalRifts.contains(0,-9,0));
        assertThrows(IllegalArgumentException.class,()->HellspecInfernalRifts.cell(BlockPos.ZERO,4049));
    }
    @Test void eruptionWidensUpwardFromThreeToTwelveBlocks(){
        assertEquals(3571,HellspecEruption.VOLUME);var seen=new HashSet<BlockPos>();int previous=0;
        for(int i=0;i<HellspecEruption.VOLUME;i++){
            var p=HellspecEruption.cell(BlockPos.ZERO,i);assertTrue(seen.add(p));assertTrue(p.getY()>=previous);previous=p.getY();
            assertTrue(HellspecEruption.contains(p.getX(),p.getY(),p.getZ()));
        }
        assertTrue(HellspecEruption.contains(12,18,0));assertFalse(HellspecEruption.contains(12,0,0));assertFalse(HellspecEruption.contains(0,-1,0));
    }
    @Test void cinderRingHas168ImmutableCellsAndLeavesItsCenterAndExterior(){
        var cells=HellspecCinderRing.cells(BlockPos.ZERO);assertEquals(168,cells.size());assertEquals(168,new HashSet<>(cells).size());
        assertTrue(cells.contains(new BlockPos(3,0,0)));assertTrue(cells.contains(new BlockPos(5,1,0)));
        assertFalse(cells.contains(BlockPos.ZERO));assertFalse(cells.contains(new BlockPos(2,0,0)));assertFalse(cells.contains(new BlockPos(6,0,0)));
        assertThrows(UnsupportedOperationException.class,()->cells.clear());
    }
}
