package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class ExcavationVolumeTest {
    @Test void vaultIsUniqueAimedFirstAndClippedOnlyAtItsRoof(){
        for(var direction:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){
            var all=new HashSet<BlockPos>();
            assertEquals(BlockPos.ZERO,ExcavationVolume.cell(BlockPos.ZERO,direction,ExcavationVolume.Shape.VAULT,0));
            for(int index=0;index<2832;index++)assertTrue(all.add(ExcavationVolume.cell(BlockPos.ZERO,direction,ExcavationVolume.Shape.VAULT,index)));
            for(int depth=0;depth<48;depth++)for(int side=-4;side<=4;side++)for(int height=0;height<7;height++){
                var cell=BlockPos.ZERO.relative(direction,depth).relative(direction.getClockWise(),side).above(height);
                assertEquals(height<6||Math.abs(side)<=2,all.contains(cell));
            }
            assertEquals(2832,all.size());
            assertFalse(all.contains(BlockPos.ZERO.relative(direction,48)));
        }
    }
    @Test void chimneyHas5808UniqueCellsAndCopiesMutableOrigin(){
        var origin=new BlockPos.MutableBlockPos(20,80,-30);var first=ExcavationVolume.cell(origin,Direction.NORTH,ExcavationVolume.Shape.CHIMNEY,0);
        origin.set(0,0,0);assertEquals(new BlockPos(20,80,-30),first);
        var all=new HashSet<BlockPos>();
        for(int index=0;index<5808;index++)assertTrue(all.add(ExcavationVolume.cell(BlockPos.ZERO,Direction.SOUTH,ExcavationVolume.Shape.CHIMNEY,index)));
        for(var cell:all)assertTrue(Math.abs(cell.getX())<=5&&Math.abs(cell.getZ())<=5&&cell.getY()>=0&&cell.getY()<48);
        assertEquals(5808,all.size());assertTrue(all.contains(new BlockPos(-5,47,5)));
    }
    @Test void geometryTranslatesExactlyWithoutChangingTraversal(){
        var origin=new BlockPos(-200,190,350);
        for(var shape:ExcavationVolume.Shape.values())for(var direction:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){
            for(int index=0;index<shape.volume;index++){
                var relative=ExcavationVolume.cell(BlockPos.ZERO,direction,shape,index);
                assertEquals(relative.offset(origin),ExcavationVolume.cell(origin,direction,shape,index));
            }
        }
    }
    @Test void invalidIndicesAndVerticalDirectionsCannotEscapeThePlan(){
        for(var shape:ExcavationVolume.Shape.values()){
            assertThrows(IllegalArgumentException.class,()->ExcavationVolume.cell(BlockPos.ZERO,Direction.SOUTH,shape,-1));
            assertThrows(IllegalArgumentException.class,()->ExcavationVolume.cell(BlockPos.ZERO,Direction.SOUTH,shape,shape.volume));
            assertThrows(IllegalArgumentException.class,()->ExcavationVolume.cell(BlockPos.ZERO,Direction.UP,shape,0));
        }
    }
}
