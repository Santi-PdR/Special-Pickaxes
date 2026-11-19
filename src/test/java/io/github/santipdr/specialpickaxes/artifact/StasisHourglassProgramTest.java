package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class StasisHourglassProgramTest {
    @Test void hourglassVisits4773UniqueCandidatesFromTheWaistOutward(){
        assertEquals(4773,StasisHourglassProgram.VOLUME);var seen=new HashSet<BlockPos>();int previous=0;
        for(int i=0;i<StasisHourglassProgram.VOLUME;i++){
            var p=StasisHourglassProgram.cell(BlockPos.ZERO,i);assertTrue(seen.add(p));
            assertTrue(StasisHourglassProgram.contains(p.getX(),p.getY(),p.getZ()));
            assertTrue(Math.abs(p.getY())>=previous);previous=Math.abs(p.getY());
        }assertEquals(BlockPos.ZERO,StasisHourglassProgram.cell(BlockPos.ZERO,0));
    }
    @Test void broadPolesDoNotTurnIntoACubeOrSphere(){
        assertFalse(StasisHourglassProgram.contains(4,0,0));assertTrue(StasisHourglassProgram.contains(7,8,0));
        assertTrue(StasisHourglassProgram.contains(12,12,0));assertFalse(StasisHourglassProgram.contains(12,12,12));
        assertFalse(StasisHourglassProgram.contains(0,13,0));
    }
    @Test void mutableOriginsAndInvalidIndicesCannotCorruptThePlan(){
        var origin=new BlockPos.MutableBlockPos(1,70,2);var first=StasisHourglassProgram.cell(origin,0);origin.set(0,0,0);
        assertEquals(new BlockPos(1,70,2),first);
        assertThrows(IllegalArgumentException.class,()->StasisHourglassProgram.cell(BlockPos.ZERO,-1));
        assertThrows(IllegalArgumentException.class,()->StasisHourglassProgram.cell(BlockPos.ZERO,4773));
    }
}
