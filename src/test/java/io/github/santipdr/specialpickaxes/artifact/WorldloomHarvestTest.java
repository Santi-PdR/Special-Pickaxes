package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class WorldloomHarvestTest {
    @Test void scanIsCenterOutAndBoundedToTheQuarryNeighborhood(){
        var cells=WorldloomHarvest.scanOffsets();assertEquals(11025,cells.size());assertEquals(cells.size(),new HashSet<>(cells).size());assertEquals(BlockPos.ZERO,cells.get(0));
        double previous=0;for(var p:cells){assertTrue(Math.abs(p.getY())<=12);assertTrue(p.getX()*p.getX()+p.getZ()*p.getZ()<=144);assertTrue(p.distSqr(BlockPos.ZERO)>=previous);previous=p.distSqr(BlockPos.ZERO);}
    }
    @Test void sharedPlanCannotBeChangedByAnActivation(){assertThrows(UnsupportedOperationException.class,()->WorldloomHarvest.scanOffsets().clear());}
}
