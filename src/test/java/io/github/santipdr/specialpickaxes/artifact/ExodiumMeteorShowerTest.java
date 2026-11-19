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
}
