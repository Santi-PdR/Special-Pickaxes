package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelicEffectsTest {
    @Test void icarianWingsFormTwoBalancedSweptTrails(){
        Vec3 center=Vec3.ZERO;
        var left=RelicEffects.icarianWingPoint(center,0,0);
        var right=RelicEffects.icarianWingPoint(center,1,0);
        var tip=RelicEffects.icarianWingPoint(center,14,0);
        assertEquals(left.x,-right.x,1.0E-9);
        assertEquals(left.y,right.y,1.0E-9);
        assertEquals(left.z,right.z,1.0E-9);
        assertTrue(Math.abs(tip.x)>Math.abs(left.x));
        assertTrue(tip.z<left.z);
        assertEquals(16,java.util.stream.IntStream.range(0,16)
                .mapToObj(i->RelicEffects.icarianWingPoint(center,i,0)).distinct().count());
    }

    @Test void icarianPhaseOffsetsOnlyShiftTheWingSweep(){
        Vec3 center=new Vec3(2,3,4);
        var ordinary=RelicEffects.icarianWingPoint(center,7,0);
        var alternate=RelicEffects.icarianWingPoint(center,7,-.18D);
        assertEquals(ordinary.x,alternate.x,1.0E-9);
        assertEquals(ordinary.y,alternate.y,1.0E-9);
        assertEquals(-.18D,alternate.z-ordinary.z,1.0E-9);
    }
}
