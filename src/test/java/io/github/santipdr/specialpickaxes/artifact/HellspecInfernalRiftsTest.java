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
}
