package io.github.santipdr.specialpickaxes.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RelicHudLayoutTest {
    @Test void ordinaryLayoutRemainsUnchanged(){
        var p=RelicHudLayout.panel(640,360,true).orElseThrow();
        assertEquals(449,p.x());assertEquals(44,p.y());assertEquals(72,p.height());assertFalse(p.compact());
    }
    @Test void allSupportedSizesAvoidHotbarAndScreenEdges(){
        for(int w=240;w<=1920;w+=17)for(int h=140;h<=1080;h+=11)for(boolean survey:new boolean[]{false,true}){
            var p=RelicHudLayout.panel(w,h,survey).orElseThrow();
            assertTrue(p.x()>=0);assertTrue(p.y()>=6);assertTrue(p.x()+p.width()<=w);
            assertTrue(p.y()+p.height()<=h-48);
            assertEquals(p.width()<168,p.compact());
        }
    }
    @Test void tinyWindowsHideRatherThanOverlap(){
        assertTrue(RelicHudLayout.panel(239,360,true).isEmpty());
        assertTrue(RelicHudLayout.panel(640,139,false).isEmpty());
    }
}
