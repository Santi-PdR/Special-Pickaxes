package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class AxiomNullChamberTest {
    @Test void octahedronHas8446UniqueCellsAndCollapsesTowardItsCore(){
        assertEquals(8446,AxiomNullChamber.VOLUME);var seen=new HashSet<BlockPos>();int previous=18;
        for(int i=0;i<AxiomNullChamber.VOLUME;i++){
            var p=AxiomNullChamber.cell(BlockPos.ZERO,i);assertTrue(seen.add(p));assertTrue(AxiomNullChamber.contains(p.getX(),p.getY(),p.getZ()));
            int distance=Math.abs(p.getX())+Math.abs(p.getY())+Math.abs(p.getZ());assertTrue(distance<=previous);previous=distance;
        }
    }
    @Test void centralCubeAndOuterCornersRemainOutsideTheExcavation(){
        for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)assertFalse(AxiomNullChamber.contains(x,y,z));
        assertTrue(AxiomNullChamber.contains(18,0,0));assertTrue(AxiomNullChamber.contains(6,6,6));assertFalse(AxiomNullChamber.contains(7,6,6));
        assertThrows(IllegalArgumentException.class,()->AxiomNullChamber.cell(BlockPos.ZERO,8446));
    }
    @Test void nullCrossHasOnlyThreeCircularPlanesAndNoCore(){
        var seen=new HashSet<BlockPos>();
        for(int i=0;i<AxiomNullCross.VOLUME;i++){
            var p=AxiomNullCross.cell(BlockPos.ZERO,i);assertTrue(seen.add(p));
            assertTrue(p.getX()==0||p.getY()==0||p.getZ()==0);assertTrue(p.distSqr(BlockPos.ZERO)<=256);
            assertTrue(Math.max(Math.max(Math.abs(p.getX()),Math.abs(p.getY())),Math.abs(p.getZ()))>1);
        }
        assertTrue(AxiomNullCross.contains(16,0,0));assertFalse(AxiomNullCross.contains(3,3,3));
    }
    @Test void aftershockIsImmutableAndPreservesTheCore(){
        var cells=AxiomAftershock.cells(BlockPos.ZERO);assertEquals(350,cells.size());assertEquals(350,new HashSet<>(cells).size());
        assertTrue(cells.contains(new BlockPos(6,0,0)));assertFalse(cells.contains(new BlockPos(1,1,1)));
        assertThrows(UnsupportedOperationException.class,()->cells.clear());
    }
}
