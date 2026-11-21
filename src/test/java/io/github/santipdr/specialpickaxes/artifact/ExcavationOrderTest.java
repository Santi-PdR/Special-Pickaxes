package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ExcavationOrderTest {
    private double old(ExcavationOrder.Style style,BlockPos at,BlockPos seed,Vec3 look){
        var d=Vec3.atCenterOf(at).subtract(Vec3.atCenterOf(seed));
        double depth=Math.max(0,d.dot(look)),distance=d.lengthSqr();
        return switch(style){
            case SEISMIC -> depth*depth*4+distance*.1;
            case TOP_DOWN -> distance*.15-d.y*12;
            case BASE_UP -> distance*.15+d.y*12;
            case DEEP -> distance-depth*depth*.8;
            case VERTICAL -> distance*.5-Math.abs(d.y)*3;
            case NEAREST -> distance;
        };
    }
    private double actual(ExcavationOrder.Style style,BlockPos at,BlockPos seed,Vec3 look){
        return ExcavationOrder.score(style,(double)at.getX()-seed.getX(),(double)at.getY()-seed.getY(),
                (double)at.getZ()-seed.getZ(),look.x,look.y,look.z);
    }
    @Test void prioritiesAreBitIdenticalToVectorMathIncludingWorldLimits(){
        var random=new Random(649);
        for(var seed:List.of(BlockPos.ZERO,new BlockPos(29999980,319,-29999980),new BlockPos(-29999980,-64,29999980)))
            for(int i=0;i<1000;i++){
                var at=seed.offset(random.nextInt(81)-40,random.nextInt(81)-40,random.nextInt(81)-40);
                var look=new Vec3(random.nextDouble()*2-1,random.nextDouble()*2-1,random.nextDouble()*2-1).normalize();
                for(var style:ExcavationOrder.Style.values())
                    assertEquals(Double.doubleToLongBits(old(style,at,seed,look)),Double.doubleToLongBits(actual(style,at,seed,look)));
            }
    }
    @Test void queueTieBreakAndFullRemovalOrderAreUnchanged(){
        var look=new Vec3(1,0,1).normalize();var seed=BlockPos.ZERO;
        for(var style:ExcavationOrder.Style.values()){
            var before=new PriorityQueue<BlockPos>(Comparator.<BlockPos>comparingDouble(p->old(style,p,seed,look)).thenComparingLong(BlockPos::asLong));
            var after=new PriorityQueue<BlockPos>(Comparator.<BlockPos>comparingDouble(p->actual(style,p,seed,look)).thenComparingLong(BlockPos::asLong));
            for(int x=-8;x<=8;x++)for(int y=-8;y<=8;y++)for(int z=-8;z<=8;z++){
                var p=new BlockPos(x,y,z);before.add(p);after.add(p);
            }
            while(!before.isEmpty())assertEquals(before.remove(),after.remove());
        }
    }
}
