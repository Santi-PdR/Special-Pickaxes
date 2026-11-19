package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class ExodiumMeteorShowerTest {
    @Test void fiveImpactBowlsAreUniqueAndExcavateTopDown(){
        assertEquals(3511,ExodiumMeteorShower.VOLUME);var seen=new HashSet<BlockPos>();int y=0;
        for(int i=0;i<ExodiumMeteorShower.VOLUME;i++){
            var p=ExodiumMeteorShower.cell(BlockPos.ZERO,i);assertTrue(seen.add(p));assertTrue(p.getY()<=y);y=p.getY();
            assertTrue(ExodiumMeteorShower.contains(p.getX(),p.getY(),p.getZ()));
            for(int above=p.getY();above<=0;above++)assertTrue(ExodiumMeteorShower.contains(p.getX(),above,p.getZ()),"every excavated column opens upward");
        }
    }
    @Test void cratersHaveDeepCentersAndDoNotExcavateOutsideTheirBowls(){
        assertTrue(ExodiumMeteorShower.contains(0,-10,0));assertTrue(ExodiumMeteorShower.contains(12,-6,0));
        assertTrue(ExodiumMeteorShower.contains(19,0,0));assertFalse(ExodiumMeteorShower.contains(19,-1,0));
        assertFalse(ExodiumMeteorShower.contains(12,0,12));assertFalse(ExodiumMeteorShower.contains(0,1,0));
        assertThrows(IllegalArgumentException.class,()->ExodiumMeteorShower.cell(BlockPos.ZERO,-1));
    }
    @Test void impactBasinIsBroadTopDownAndContains6789UniqueCells(){
        assertEquals(6789,ExodiumImpactBasin.VOLUME);var seen=new HashSet<BlockPos>();int previous=0;
        for(int i=0;i<ExodiumImpactBasin.VOLUME;i++){
            var p=ExodiumImpactBasin.cell(BlockPos.ZERO,i);assertTrue(seen.add(p));assertTrue(p.getY()<=previous);previous=p.getY();
            assertTrue(ExodiumImpactBasin.contains(p.getX(),p.getY(),p.getZ()));
            for(int y=p.getY();y<=0;y++)assertTrue(ExodiumImpactBasin.contains(p.getX(),y,p.getZ()));
        }
        assertTrue(ExodiumImpactBasin.contains(0,-16,0));assertFalse(ExodiumImpactBasin.contains(16,-1,0));
    }
    @Test void curiosFallingStarHas201ImmutableCellsInASmallBowl(){
        var cells=ExodiumFallingStar.cells(BlockPos.ZERO);assertEquals(201,cells.size());assertEquals(201,new HashSet<>(cells).size());
        assertTrue(cells.contains(new BlockPos(0,-4,0)));assertTrue(cells.contains(new BlockPos(5,0,0)));
        assertFalse(cells.contains(new BlockPos(5,-1,0)));assertFalse(cells.contains(new BlockPos(0,1,0)));
        assertThrows(UnsupportedOperationException.class,()->cells.clear());
    }
}
