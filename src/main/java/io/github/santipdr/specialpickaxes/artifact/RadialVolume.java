package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;

/** Shared world-free plans: ability activation and each scheduler peek are constant-time. */
public final class RadialVolume {
    public static final int MAX_RADIUS=12;
    public enum Shape { SPHERE, SHELL, BLOOM, CUBE }
    private static final List<List<List<BlockPos>>> PLANS=createPlans();
    private RadialVolume(){}

    public static void warmup(){PLANS.size();}
    public static List<BlockPos> offsets(Shape shape,int radius){
        return PLANS.get(shape.ordinal()).get(Math.max(0,Math.min(MAX_RADIUS,radius)));
    }
    private static List<List<List<BlockPos>>> createPlans(){
        var universe=new ArrayList<BlockPos>();
        // One immutable coordinate object per cube cell, reused across every shape/radius.
        for(int shell=0;shell<=MAX_RADIUS;shell++)for(int x=-shell;x<=shell;x++)
            for(int y=-shell;y<=shell;y++)for(int z=-shell;z<=shell;z++)
                if(Math.max(Math.max(Math.abs(x),Math.abs(y)),Math.abs(z))==shell)universe.add(new BlockPos(x,y,z));
        var shapes=new ArrayList<List<List<BlockPos>>>();
        for(var shape:Shape.values()){
            var radii=new ArrayList<List<BlockPos>>();
            for(int radius=0;radius<=MAX_RADIUS;radius++)radii.add(create(shape,radius,universe));
            shapes.add(List.copyOf(radii));
        }
        return List.copyOf(shapes);
    }
    private static List<BlockPos> create(Shape shape,int radius,List<BlockPos> universe){
        var cells=new ArrayList<BlockPos>();
        // Preserve the historical Chebyshev-shell / x / y / z traversal exactly.
        for(var pos:universe){
            int x=pos.getX(),y=pos.getY(),z=pos.getZ();
            if(Math.max(Math.max(Math.abs(x),Math.abs(y)),Math.abs(z))>radius)break;
            int distance=x*x+y*y+z*z,inner=Math.max(0,radius-2);
            boolean inside=switch(shape){
                case SPHERE -> distance<=radius*radius;
                case SHELL -> distance<=radius*radius&&distance>=inner*inner;
                case BLOOM -> HellspecBloomShape.contains(x,y,z);
                case CUBE -> true;
            };
            if(inside)cells.add(pos);
        }
        return List.copyOf(cells);
    }
}
