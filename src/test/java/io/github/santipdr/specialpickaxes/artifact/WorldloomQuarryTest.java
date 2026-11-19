package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class WorldloomQuarryTest {
    @Test void fourTerracesHave4164UniqueTopDownCandidates(){
        assertEquals(4164,WorldloomQuarry.VOLUME);var seen=new HashSet<BlockPos>();int depth=0;
        for(int i=0;i<WorldloomQuarry.VOLUME;i++){
            var p=WorldloomQuarry.cell(BlockPos.ZERO,i);assertTrue(seen.add(p));
            assertTrue(WorldloomQuarry.contains(p.getX(),p.getY(),p.getZ()));assertTrue(-p.getY()>=depth);depth=-p.getY();
        }assertEquals(BlockPos.ZERO,WorldloomQuarry.cell(BlockPos.ZERO,0));assertEquals(11,depth);
    }
    @Test void quarryHasNoCeilingOrDirectionalTunnel(){
        assertTrue(WorldloomQuarry.contains(12,0,0));assertFalse(WorldloomQuarry.contains(12,-3,0));
        assertTrue(WorldloomQuarry.contains(9,-11,0));assertFalse(WorldloomQuarry.contains(10,-11,0));
        assertFalse(WorldloomQuarry.contains(0,1,0));assertFalse(WorldloomQuarry.contains(0,-12,0));
        assertThrows(IllegalArgumentException.class,()->WorldloomQuarry.cell(BlockPos.ZERO,4164));
    }
}
