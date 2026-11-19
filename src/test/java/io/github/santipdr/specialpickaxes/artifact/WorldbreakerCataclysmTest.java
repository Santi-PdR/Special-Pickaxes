package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
class WorldbreakerCataclysmTest {
    @Test void chamberHasUniqueTopDownCellsAndBeveledCorners(){
        var seen=new HashSet<BlockPos>();int previous=8;
        for(int i=0;i<WorldbreakerCataclysm.VOLUME;i++){
            var p=WorldbreakerCataclysm.cell(BlockPos.ZERO,i);assertTrue(seen.add(p));assertTrue(p.getY()<=previous);previous=p.getY();
            assertTrue(WorldbreakerCataclysm.contains(p.getX(),p.getY(),p.getZ()));
        }
        assertTrue(WorldbreakerCataclysm.VOLUME>8192);
        assertTrue(WorldbreakerCataclysm.contains(16,0,8));assertFalse(WorldbreakerCataclysm.contains(16,0,9));
        assertTrue(WorldbreakerCataclysm.contains(16,8,4));assertFalse(WorldbreakerCataclysm.contains(16,8,5));
    }
    @Test void chamberBoundsIncludeCenterButNeverExtendIntoAForwardTunnel(){
        assertTrue(WorldbreakerCataclysm.contains(0,0,0));assertFalse(WorldbreakerCataclysm.contains(0,0,17));
        assertFalse(WorldbreakerCataclysm.contains(0,9,0));
        assertThrows(IllegalArgumentException.class,()->WorldbreakerCataclysm.cell(BlockPos.ZERO,WorldbreakerCataclysm.VOLUME));
    }
    @Test void continentalShearHas5379UniqueCellsOnExactlyThreeDiscs(){
        assertEquals(5379,WorldbreakerContinentalShear.VOLUME);var seen=new HashSet<BlockPos>();int previous=6;
        for(int i=0;i<WorldbreakerContinentalShear.VOLUME;i++){
            var p=WorldbreakerContinentalShear.cell(BlockPos.ZERO,i);assertTrue(seen.add(p));assertTrue(p.getY()<=previous);previous=p.getY();
            assertTrue(p.getY()==-6||p.getY()==0||p.getY()==6);assertTrue(p.getX()*p.getX()+p.getZ()*p.getZ()<=576);
        }
        assertTrue(WorldbreakerContinentalShear.contains(24,6,0));assertFalse(WorldbreakerContinentalShear.contains(24,1,0));
        assertFalse(WorldbreakerContinentalShear.contains(24,0,1));
    }
}
