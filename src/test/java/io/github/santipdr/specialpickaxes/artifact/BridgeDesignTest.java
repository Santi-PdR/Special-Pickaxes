package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class BridgeDesignTest {
    @Test void flatAscentAndDescentEachHave64ContinuousUniquePaidCells(){
        for(var direction:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST})for(int slope=-1;slope<=1;slope++){
            var seen=new HashSet<BlockPos>();var floor=new BlockPos(10,100,-10);BlockPos previous=null;
            for(int depth=1;depth<=64;depth++){
                var at=BridgeDesign.cell(floor,direction,depth,slope);assertTrue(seen.add(at));
                assertEquals(floor.getY()+slope*(depth-1),at.getY());
                if(previous!=null){assertEquals(slope,at.getY()-previous.getY());assertEquals(1,Math.abs(at.getX()-previous.getX())+Math.abs(at.getZ()-previous.getZ()));}
                previous=at;
            }
        }
    }
    @Test void fullBlocksStayFlatRegardlessOfLookAndStairsSelectADirection(){
        for(float pitch:new float[]{-90,-45,0,35,60,90,Float.NaN})assertEquals(0,BridgeDesign.slope(pitch,false));
        assertEquals(1,BridgeDesign.slope(-45,true));assertEquals(1,BridgeDesign.slope(0,true));
        assertEquals(-1,BridgeDesign.slope(60,true));assertEquals(1,BridgeDesign.slope(Float.NaN,true));
    }
    @Test void malformedPlansCannotBuildUnboundedOrVerticalColumns(){
        assertThrows(IllegalArgumentException.class,()->BridgeDesign.cell(BlockPos.ZERO,Direction.SOUTH,0,0));
        assertThrows(IllegalArgumentException.class,()->BridgeDesign.cell(BlockPos.ZERO,Direction.SOUTH,65,0));
        assertThrows(IllegalArgumentException.class,()->BridgeDesign.cell(BlockPos.ZERO,Direction.UP,1,0));
        assertThrows(IllegalArgumentException.class,()->BridgeDesign.cell(BlockPos.ZERO,Direction.SOUTH,1,2));
    }
}
