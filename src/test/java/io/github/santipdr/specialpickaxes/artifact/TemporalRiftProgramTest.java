package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class TemporalRiftProgramTest {
    @Test void everyCellOfTheElevenCubedVolumeIsVisitedExactlyOnce(){
        var cells=new HashSet<BlockPos>();
        for(int i=0;i<TemporalRiftProgram.VOLUME;i++)assertTrue(cells.add(TemporalRiftProgram.cell(BlockPos.ZERO,i)));
        assertEquals(1331,cells.size());
        for(var p:cells)assertTrue(Math.abs(p.getX())<=5&&Math.abs(p.getY())<=5&&Math.abs(p.getZ())<=5);
        assertTrue(cells.contains(new BlockPos(-5,-5,-5))&&cells.contains(new BlockPos(5,5,5)));
    }
    @Test void theAimedBlockIsFirstAndMutableOriginsAreNotRetained(){
        var origin=new BlockPos.MutableBlockPos(20,70,-30);var result=TemporalRiftProgram.cell(origin,0);origin.set(0,0,0);
        assertEquals(new BlockPos(20,70,-30),result);
    }
    @Test void invalidCellIndicesCannotEscapeThePlan(){
        assertThrows(IllegalArgumentException.class,()->TemporalRiftProgram.cell(BlockPos.ZERO,-1));
        assertThrows(IllegalArgumentException.class,()->TemporalRiftProgram.cell(BlockPos.ZERO,1331));
    }
}
