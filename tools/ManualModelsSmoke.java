import io.github.santipdr.specialpickaxes.artifact.ManualChoiceNavigation;
import io.github.santipdr.specialpickaxes.artifact.ManualScreenLayout;
import io.github.santipdr.specialpickaxes.artifact.ManualScrollModel;
import io.github.santipdr.specialpickaxes.artifact.ManualWheelScroll;
import java.util.Random;

/** Executes production Java models without Minecraft, Gradle or a third-party test runner. */
public final class ManualModelsSmoke {
    private static int checks;
    private static void check(boolean condition,String description){
        checks++;
        if(!condition)throw new AssertionError(description);
    }
    private static void eq(int expected,int actual,String description){
        check(expected==actual,description+": expected="+expected+", actual="+actual);
    }
    private static void wheel(){
        var wheel=new ManualWheelScroll();int position=0;
        for(int i=0;i<8;i++)position=wheel.move(position,100,-.125,4);
        eq(4,position,"fractional accumulation from top edge");
        eq(32,wheel.move(position,100,-1,28),"ordinary mouse notch");
        wheel.reset();
        eq(0,wheel.move(0,100,.125,4),"outward top edge");
        eq(1,wheel.move(0,100,-.25,4),"inward top edge has no debt");
        eq(100,wheel.move(100,100,-.125,4),"outward bottom edge");
        eq(99,wheel.move(100,100,.25,4),"inward bottom edge has no debt");
        wheel.reset();
        eq(50,wheel.move(50,100,-.125,4),"subpixel movement");
        eq(49,wheel.move(50,100,.25,4),"direction reversal");
        wheel.move(50,100,-.125,4);wheel.reset();
        eq(50,wheel.move(50,100,-.125,4),"reset clears residue");
        for(double delta:new double[]{Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY})
            eq(50,wheel.move(50,100,delta,28),"non-finite input");
        eq(Integer.MAX_VALUE,wheel.move(10,Integer.MAX_VALUE,-Double.MAX_VALUE,28),"huge finite movement down");
        eq(0,wheel.move(10,Integer.MAX_VALUE,Double.MAX_VALUE,28),"huge finite movement up");
        eq(78,wheel.move(50,100,-1,28),"valid after invalid input");
        eq(0,wheel.move(-100,-1,-1,28),"empty viewport");
        var content=new ManualWheelScroll();var picker=new ManualWheelScroll();
        eq(0,content.move(0,100,-.125,4),"content partial");
        eq(0,picker.move(0,100,-.125,4),"picker independent");
        eq(1,content.move(0,100,-.125,4),"content residue retained");
        var random=new Random(5106L);
        for(int i=0;i<10000;i++){
            int max=random.nextInt(Integer.MAX_VALUE);
            int result=wheel.move(random.nextInt(),max,random.nextGaussian()*100,28);
            check(result>=0&&result<=max,"random wheel bounds");
        }
    }
    private static void layouts(){
        for(int width:new int[]{0,1,16,90,160,213,320,426,640,854,1280,1920,Integer.MAX_VALUE})
            for(int height:new int[]{0,1,16,90,120,180,240,360,480,720,1080,Integer.MAX_VALUE})
                for(boolean picker:new boolean[]{false,true}){
                    var layout=ManualScreenLayout.calculate(width,height,picker);
                    var panel=layout.panel();var content=layout.content();
                    check(panel.x()>=0&&panel.y()>=0,"panel origin");
                    check(panel.right()<=width&&panel.bottom()<=height,"panel fits screen");
                    check(panel.height()<=260&&panel.width()<=760,"compact cap");
                    check(content.width()>=0&&content.height()>=0,"nonnegative viewport");
                    check(content.x()>=panel.x()&&content.right()<=panel.right(),"content horizontal containment");
                    check(content.y()>=panel.y()&&content.bottom()<=layout.footerY(),"content clears footer");
                    if(layout.pickerVisible())check(layout.picker().bottom()<=content.y(),"picker clears content");
                    if(layout.twoColumns())check(layout.firstColumn().right()<layout.secondColumn().x(),"columns separated");
                }
        eq(-1,ManualScreenLayout.choiceIndexAt(Integer.MAX_VALUE,Integer.MAX_VALUE,24,13),"picker offset overflow");
        eq(2,ManualScreenLayout.choiceIndexAt(1,48,24,13),"scrolled picker maps to source");
        eq(-1,ManualScreenLayout.choiceIndexAt(23,0,24,13),"picker gap is not a choice");
    }
    private static void scrollbars(){
        eq(0,ManualScrollModel.maxScroll(Integer.MIN_VALUE,100),"negative content");
        for(int track=1;track<=260;track++){
            int max=ManualScrollModel.maxScroll(2000,track);
            int thumb=ManualScrollModel.thumbHeight(track,2000),travel=track-thumb;
            check(thumb>=1&&thumb<=track,"reachable thumb");
            eq(0,ManualScrollModel.thumbOffset(track,2000,0),"thumb top");
            eq(travel,ManualScrollModel.thumbOffset(track,2000,max),"thumb bottom");
            if(travel>0){
                eq(0,ManualScrollModel.scrollFromThumb(track,2000,Integer.MIN_VALUE,0),"extreme drag top");
                eq(max,ManualScrollModel.scrollFromThumb(track,2000,Integer.MAX_VALUE,0),"extreme drag bottom");
            }
        }
    }
    private static void choiceNavigation(){
        eq(-1,ManualChoiceNavigation.step(0,1,0),"empty picker");
        eq(0,ManualChoiceNavigation.step(0,-1,13),"left edge does not wrap");
        eq(12,ManualChoiceNavigation.step(12,1,13),"right edge does not wrap");
        for(int viewport=24;viewport<=760;viewport++)for(int index=0;index<13;index++){
            int scroll=ManualChoiceNavigation.reveal(212,index,13,24,viewport);
            check(scroll>=0&&scroll<=Math.max(0,312-viewport),"picker scroll bounded");
            check(index*24>=scroll&&(index+1)*24<=scroll+viewport,"focused choice visible");
        }
    }
    public static void main(String[] args){
        wheel();layouts();scrollbars();choiceNavigation();
        System.out.println("MANUAL_MODELS_OK checks="+checks+" scope=pure_java_only");
    }
}
