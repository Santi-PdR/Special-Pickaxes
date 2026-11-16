package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManualResponsiveLayoutTest {
    @Test void allNormalGuiScalesKeepContentPickerAndFooterInsideThePanel(){
        for(int width:new int[]{160,213,320,426,640,854,1280,1920})
            for(int height:new int[]{90,120,180,240,360,480,720,1080})
                for(boolean picker:new boolean[]{false,true}){
                    var layout=ManualScreenLayout.calculate(width,height,picker);
                    var panel=layout.panel();var content=layout.content();
                    assertTrue(panel.x()>=0&&panel.y()>=0);
                    assertTrue(panel.right()<=width&&panel.bottom()<=height);
                    assertTrue(panel.height()<=260);
                    assertTrue(content.width()>=0&&content.height()>=0);
                    assertTrue(content.x()>=panel.x()&&content.right()<=panel.right());
                    assertTrue(content.y()>=panel.y()&&content.bottom()<=layout.footerY());
                    if(layout.pickerVisible())assertTrue(layout.picker().bottom()<=content.y());
                    if(layout.twoColumns())assertTrue(layout.firstColumn().right()<layout.secondColumn().x());
                }
    }
    @Test void compactDesktopPanelDoesNotFillMostOfTheScreen(){
        var panel=ManualScreenLayout.calculate(1280,720,false).panel();
        assertEquals(260,panel.height());
        assertEquals(230,panel.y());
    }
    @Test void longTranslatedContentStillScrollsToBothEndsAtEveryTrackHeight(){
        for(int track=1;track<=260;track++){
            int content=2000,max=ManualScrollModel.maxScroll(content,track);
            int thumb=ManualScrollModel.thumbHeight(track,content),travel=track-thumb;
            assertTrue(thumb>=1&&thumb<=track);
            assertEquals(0,ManualScrollModel.thumbOffset(track,content,0));
            assertEquals(travel,ManualScrollModel.thumbOffset(track,content,max));
            if(travel>0){
                assertEquals(0,ManualScrollModel.scrollFromThumb(track,content,0,0));
                assertEquals(max,ManualScrollModel.scrollFromThumb(track,content,travel,0));
            }
        }
    }
}
