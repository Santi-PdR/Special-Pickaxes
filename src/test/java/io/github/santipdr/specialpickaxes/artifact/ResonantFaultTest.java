package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class ResonantFaultTest {
    @Test void fanExpandsToFortyNineWideWithoutTunnelWalls(){
        var cells=ResonantFault.geometry(BlockPos.ZERO,Direction.SOUTH,ResonantFault.Form.FAN);
        assertEquals(5310,cells.size());assertEquals(cells.size(),new HashSet<>(cells).size());assertEquals(BlockPos.ZERO,cells.get(0));
        assertTrue(cells.contains(new BlockPos(24,2,31)));assertTrue(cells.contains(new BlockPos(-12,-2,10)));
        assertFalse(cells.contains(new BlockPos(3,0,0)));assertFalse(cells.contains(new BlockPos(0,3,10)));
    }
    @Test void cascadeHasFourSeparateVerticalFaultsNotASolidBore(){
        var cells=ResonantFault.geometry(BlockPos.ZERO,Direction.SOUTH,ResonantFault.Form.CASCADE);
        assertEquals(4100,cells.size());assertEquals(cells.size(),new HashSet<>(cells).size());assertEquals(BlockPos.ZERO,cells.get(0));
        assertTrue(cells.contains(new BlockPos(20,12,18)));assertFalse(cells.contains(new BlockPos(0,0,5)));
        for(var p:cells)assertEquals(0,p.getZ()%6);
    }
    @Test void replicaIsNinetyNineUniqueCellsAndAllPlansRotateExactly(){
        var origin=new BlockPos(-15,90,16);
        for(var form:ResonantFault.Form.values()){
            var south=ResonantFault.geometry(BlockPos.ZERO,Direction.SOUTH,form);
            if(form==ResonantFault.Form.REPLICA)assertEquals(99,south.size());
            for(var d:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){
                var rotated=ResonantFault.geometry(origin,d,form);assertEquals(south.size(),new HashSet<>(rotated).size());
                for(int i=0;i<south.size();i++){var p=south.get(i);assertEquals(origin.relative(d.getClockWise(),-p.getX()).relative(d,p.getZ()).above(p.getY()),rotated.get(i));}
                assertThrows(UnsupportedOperationException.class,rotated::clear);
            }
        }
        assertThrows(IllegalArgumentException.class,()->ResonantFault.geometry(origin,Direction.UP,ResonantFault.Form.FAN));
    }
}
