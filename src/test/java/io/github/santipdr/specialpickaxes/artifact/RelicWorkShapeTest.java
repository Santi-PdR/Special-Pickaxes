package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class RelicWorkShapeTest {
    @Test void everyWorkAndFinishPatternIsDistinctFiniteAndBounded(){
        for(boolean complete:new boolean[]{false,true}){
            var signatures=new HashSet<String>();
            int count=complete?RelicWorkShape.FINISH_COUNT:RelicWorkShape.WORK_COUNT;
            for(var style:AbilityBurstShape.Shape.values()){
                var points=new HashSet<Vec3>();var signature=new StringBuilder();
                for(int i=0;i<count;i++){
                    var point=RelicWorkShape.point(style,i,complete);
                    assertTrue(Double.isFinite(point.lengthSqr()),style.name());
                    assertTrue(point.lengthSqr()<5,style.name());
                    assertTrue(points.add(point),style.name()+" duplicate "+i);
                    signature.append(point);
                }
                assertTrue(signatures.add(signature.toString()),style.name()+" shares another tool's impact");
            }
        }
    }
    @Test void invalidIndicesCannotGenerateUnboundedWork(){
        for(boolean complete:new boolean[]{false,true}){
            assertThrows(IllegalArgumentException.class,()->RelicWorkShape.point(AbilityBurstShape.Shape.CHOIR,-1,complete));
            int count=complete?RelicWorkShape.FINISH_COUNT:RelicWorkShape.WORK_COUNT;
            assertThrows(IllegalArgumentException.class,()->RelicWorkShape.point(AbilityBurstShape.Shape.CHOIR,count,complete));
        }
    }
    @Test void cachedPointsRetainEveryOriginalCoordinateAndStayImmutable(){
        for(var style:AbilityBurstShape.Shape.values())for(boolean complete:new boolean[]{false,true}){
            int count=complete?RelicWorkShape.FINISH_COUNT:RelicWorkShape.WORK_COUNT;
            for(int i=0;i<count;i++){
                var expected=originalPoint(style,i,complete);var actual=RelicWorkShape.point(style,i,complete);
                assertEquals(Double.doubleToLongBits(expected.x),Double.doubleToLongBits(actual.x));
                assertEquals(Double.doubleToLongBits(expected.y),Double.doubleToLongBits(actual.y));
                assertEquals(Double.doubleToLongBits(expected.z),Double.doubleToLongBits(actual.z));
                assertSame(actual,RelicWorkShape.point(style,i,complete));
                actual.add(100,200,300).scale(2); // Consumers create transformed values, never mutate shared local points.
                assertEquals(expected,RelicWorkShape.point(style,i,complete));
            }
        }
    }
    // Independent pre-cache implementation: catches changes to cached coordinates/order.
    private static Vec3 originalPoint(AbilityBurstShape.Shape style,int index,boolean complete){
        int count=complete?RelicWorkShape.FINISH_COUNT:RelicWorkShape.WORK_COUNT;
        if(index<0||index>=count)throw new IllegalArgumentException("impact index");
        double t=index/(double)(count-1),a=index*Math.PI*2/count;
        double r=complete?1.2:.38;
        return switch(style){
            case PALIMPSEST -> new Vec3(Math.cos(a)*r,complete?.2+t*.6:.1,Math.sin(a)*r);
            case CHOIR -> new Vec3((t-.5)*r*2,Math.sin(t*Math.PI*3)*r*.35,complete?.25:.05);
            case EVENTIDE -> new Vec3(Math.cos(a)*(1-t*.65)*r,Math.sin(a)*r*.5,Math.sin(a)*(1-t*.65)*r);
            case CRUCIBLE -> new Vec3((index%4-1.5)*r*.6,(index/4-(complete?1.5:.5))*r*.6,.08);
            case INTERREGNUM -> new Vec3(Math.cos(a)*r,Math.sin(a)*r,(index%2)*.12);
            case WORLDLOOM -> new Vec3((index%2==0?-1:1)*r*t,.08,Math.sin(t*Math.PI*2)*r*.3+t*r);
            case ICARUS -> new Vec3((index%2==0?-1:1)*(.1+t*r),t*r*.6,-t*r*.4);
            case AXIOM -> new Vec3(Math.cos(a)*r,Math.sin(a)*r*.5,Math.cos(a*3)*r*.2);
            case WORLDBREAKER -> new Vec3(Math.cos(a)*(index%2==0?r:r*.45),Math.sin(a)*(index%2==0?r:r*.45),.1);
            case EXODIUM -> new Vec3(Math.cos(a)*r*(1-t*.5),r*(1-t),Math.sin(a)*r*(1-t*.5));
            case IRIDIUM -> new Vec3(Math.cos(a)*r,Math.abs(Math.sin(a))*r*.6,Math.sin(a)*r);
            case HELLSPEC -> new Vec3(Math.cos(a)*r*(.7+.3*Math.cos(a*5)),t*r*.8,Math.sin(a)*r*(.7+.3*Math.cos(a*5)));
            case SEAM_RIPPER -> new Vec3((t-.5)*r*2,(index%2==0?-1:1)*r*.22,.05);
        };
    }
}
