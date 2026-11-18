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
}
