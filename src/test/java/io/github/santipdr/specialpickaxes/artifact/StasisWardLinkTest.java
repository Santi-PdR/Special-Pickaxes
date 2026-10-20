package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StasisWardLinkTest {
    @Test void stasisLinkSamplesStayBetweenBothPlayers(){
        Vec3 start=new Vec3(1,2,3),end=new Vec3(11,12,13);
        for(int i=1;i<=4;i++){
            Vec3 point=StasisWardLinkGeometry.point(start,end,i);
            assertEquals(i/5D,point.distanceTo(start)/start.distanceTo(end),1.0E-9);
            assertTrue(point.distanceTo(end)<start.distanceTo(end));
        }
    }

    @Test void stasisLinkClampsSamplesAwayFromPlayerSilhouettes(){
        Vec3 start=Vec3.ZERO,end=new Vec3(10,0,0);
        assertEquals(2D,StasisWardLinkGeometry.point(start,end,-7).x,1.0E-9);
        assertEquals(8D,StasisWardLinkGeometry.point(start,end,99).x,1.0E-9);
    }
}
