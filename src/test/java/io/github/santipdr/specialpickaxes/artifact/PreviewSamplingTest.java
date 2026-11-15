package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PreviewSamplingTest {
    @Test void previewsIncludeTheBeginningAndEndEvenJustBelowTheOldStrideBoundary(){
        for(int size:new int[]{25,47,49,108,4096,Integer.MAX_VALUE}){
            assertEquals(0,PreviewSampling.index(size,0,24));
            assertEquals(size-1,PreviewSampling.index(size,23,24));
        }
    }
    @Test void smallPlansDisplayEveryCellWithoutDuplicates(){
        for(int size=1;size<=24;size++)for(int sample=0;sample<size;sample++)
            assertEquals(sample,PreviewSampling.index(size,sample,size));
    }
    @Test void longPlanSamplesRemainUniqueOrderedAndUniform(){
        for(int size:new int[]{25,47,49,108,4096,Integer.MAX_VALUE}){
            int previous=-1,minimum=Integer.MAX_VALUE,maximum=0;
            for(int sample=0;sample<24;sample++){
                int index=PreviewSampling.index(size,sample,24);
                assertTrue(index>previous&&index<size);
                if(sample>0){minimum=Math.min(minimum,index-previous);maximum=Math.max(maximum,index-previous);}
                previous=index;
            }
            assertTrue(maximum-minimum<=1);
        }
    }
    @Test void largeMultiplicationNeverWrapsIntoANegativeIndex(){
        assertEquals(Integer.MAX_VALUE-1,PreviewSampling.index(Integer.MAX_VALUE,Integer.MAX_VALUE-1,Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE-2,PreviewSampling.index(Integer.MAX_VALUE,Integer.MAX_VALUE-2,Integer.MAX_VALUE));
    }
    @Test void invalidArgumentsNeverPretendToSelectARealCell(){
        for(int[] args:new int[][]{{0,0,0},{-1,0,1},{10,-1,5},{10,5,5},{10,0,11},{10,0,0}})
            assertThrows(IllegalArgumentException.class,()->PreviewSampling.index(args[0],args[1],args[2]));
    }
}
