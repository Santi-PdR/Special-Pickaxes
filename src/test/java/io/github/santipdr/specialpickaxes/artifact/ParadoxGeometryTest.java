package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ParadoxGeometryTest {
    @Test void twelveOrdersAreBijectionsAndReverseTheirMatchingForwardOrder(){
        var volume=new SelectionVolume(new BlockPos(-3,60,15),new BlockPos(-1,63,19));
        var routes=new HashSet<List<BlockPos>>();
        for(int mode=0;mode<12;mode++){
            var cells=new ArrayList<BlockPos>();for(int i=0;i<volume.size();i++)cells.add(ParadoxOrder.at(volume,i,mode));
            assertEquals(volume.size(),new HashSet<>(cells).size());assertTrue(cells.stream().allMatch(volume::contains));routes.add(cells);
            if(mode>=6)for(int i=0;i<volume.size();i++)assertEquals(ParadoxOrder.at(volume,volume.size()-1-i,mode-6),cells.get(i));
        }assertEquals(12,routes.size());
    }
    @Test void latticeHas6101UniqueCellsAndLeavesCausalGaps(){
        var cells=ParadoxLattice.lattice(BlockPos.ZERO);assertEquals(6101,cells.size());assertEquals(cells.size(),new HashSet<>(cells).size());assertEquals(BlockPos.ZERO,cells.get(0));
        assertTrue(cells.contains(new BlockPos(1,1,0)));assertFalse(cells.contains(new BlockPos(1,0,0)));assertFalse(cells.contains(new BlockPos(14,1,0)));
        assertThrows(UnsupportedOperationException.class,cells::clear);
    }
    @Test void fourEchoesAreDeduplicatedAndBoundedTo108Candidates(){
        var scars=List.of(BlockPos.ZERO,new BlockPos(5,0,0),new BlockPos(10,0,0),new BlockPos(15,0,0));
        assertEquals(108,ParadoxLattice.echoes(scars).size());assertEquals(27,ParadoxLattice.echoes(Collections.nCopies(4,BlockPos.ZERO)).size());
    }
}
