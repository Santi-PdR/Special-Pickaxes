package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Shared block geometry for Hellspec's mined petals and their 3D preview. */
public final class HellspecBloomShape {
    private static final int[][] PETALS={{0,0},{4,0},{-4,0},{0,4},{0,-4}};
    private HellspecBloomShape(){}

    public static boolean contains(int dx,int dy,int dz){
        int vertical=2*dy*dy;
        for(int[] petal:PETALS){int x=dx-petal[0],z=dz-petal[1];if(x*x+z*z+vertical<=9)return true;}
        return false;
    }

    /** Exact 6-face boundary of the bounded 3D carve, expressed relative to its center. */
    public static List<BlockPos> boundary(){
        Set<BlockPos> volume=new HashSet<>();
        for(int x=-7;x<=7;x++)for(int y=-2;y<=2;y++)for(int z=-7;z<=7;z++)
            if(contains(x,y,z))volume.add(new BlockPos(x,y,z));
        List<BlockPos> outline=new ArrayList<>();
        for(BlockPos pos:volume){
            for(Direction direction:Direction.values())if(!volume.contains(pos.relative(direction))){outline.add(pos);break;}
        }
        outline.sort((a,b)->{int y=Integer.compare(a.getY(),b.getY());if(y!=0)return y;int x=Integer.compare(a.getX(),b.getX());return x!=0?x:Integer.compare(a.getZ(),b.getZ());});
        return List.copyOf(outline);
    }
}
