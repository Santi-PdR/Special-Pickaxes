package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class SolarRuptureTest {
    @Test void raysAreEightThickArmsNotAVeinOrTunnel(){
        var cells=SolarRupture.offsets(SolarRupture.Form.RAYS);
        assertEquals(2765,cells.size());assertEquals(cells.size(),new HashSet<>(cells).size());
        assertTrue(cells.contains(new BlockPos(24,2,24)));assertTrue(cells.contains(new BlockPos(0,-2,24)));
        assertFalse(cells.contains(new BlockPos(12,0,6)));assertFalse(cells.contains(new BlockPos(0,3,0)));
    }
    @Test void novaIsACompleteTwelveBlockSphere(){
        var cells=SolarRupture.offsets(SolarRupture.Form.NOVA);assertEquals(7153,cells.size());
        assertEquals(cells.size(),new HashSet<>(cells).size());
        for(var p:cells){assertTrue(p.distSqr(BlockPos.ZERO)<=144);assertTrue(cells.contains(new BlockPos(-p.getX(),-p.getY(),-p.getZ())));}
    }
    @Test void bothPlansAreNearestFirstAndImmutable(){
        for(var form:SolarRupture.Form.values()){
            var cells=SolarRupture.offsets(form);assertEquals(BlockPos.ZERO,cells.get(0));
            for(int i=1;i<cells.size();i++)assertTrue(cells.get(i-1).distSqr(BlockPos.ZERO)<=cells.get(i).distSqr(BlockPos.ZERO));
            assertThrows(UnsupportedOperationException.class,()->cells.clear());
        }
    }
}
