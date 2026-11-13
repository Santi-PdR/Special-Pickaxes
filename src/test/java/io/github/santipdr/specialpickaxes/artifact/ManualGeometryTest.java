package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManualGeometryTest {
    @Test void negativeContentCannotOverflowIntoAPositiveScroll(){
        assertEquals(0,ManualScrollModel.maxScroll(Integer.MIN_VALUE,100));
        assertEquals(0,ManualScrollModel.maxScroll(-1,Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE,ManualScrollModel.maxScroll(Integer.MAX_VALUE,-1));
    }
    @Test void draggingExtremeCoordinatesClampsToTheCorrectEdge(){
        int max=ManualScrollModel.maxScroll(1000,100);
        assertEquals(max,ManualScrollModel.scrollFromThumb(100,1000,Integer.MAX_VALUE,-100));
        assertEquals(0,ManualScrollModel.scrollFromThumb(100,1000,Integer.MIN_VALUE,100));
        assertEquals(0,ManualScrollModel.thumbOffset(100,1000,Integer.MIN_VALUE));
        assertEquals(90,ManualScrollModel.thumbOffset(100,1000,Integer.MAX_VALUE));
    }
    @Test void iconOffsetCannotOverflowIntoANegativeIndex(){
        assertEquals(-1,ManualScreenLayout.choiceIndexAt(Integer.MAX_VALUE,Integer.MAX_VALUE,24,13));
        assertEquals(0,ManualScreenLayout.choiceIndexAt(1,0,24,13));
        assertEquals(2,ManualScreenLayout.choiceIndexAt(1,48,24,13));
        assertEquals(-1,ManualScreenLayout.choiceIndexAt(23,0,24,13));
    }
    @Test void negativeScreenDimensionsBehaveAsAnEmptyViewport(){
        assertEquals(ManualScreenLayout.calculate(0,0,true),
                ManualScreenLayout.calculate(Integer.MIN_VALUE,Integer.MIN_VALUE,true));
    }
    @Test void scrollbarRoundTripsStayBoundedAcrossOrdinaryLayouts(){
        for(int track:new int[]{1,10,100,540})for(int content:new int[]{0,5,800,Integer.MAX_VALUE}){
            int max=ManualScrollModel.maxScroll(content,track);
            int offset=ManualScrollModel.thumbOffset(track,content,max);
            int restored=ManualScrollModel.scrollFromThumb(track,content,offset,0);
            assertTrue(restored>=0&&restored<=max);
            if(track>ManualScrollModel.thumbHeight(track,content))assertEquals(max,restored);
        }
    }
}
