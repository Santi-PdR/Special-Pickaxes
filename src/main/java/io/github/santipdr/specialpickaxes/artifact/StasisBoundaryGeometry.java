package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Pure positions for the small client-rendered markers on the Stasis field's upper edge. */
public final class StasisBoundaryGeometry {
    private StasisBoundaryGeometry() {}

    public static Vec3 upperCorner(BlockPos center,int radius,int halfHeight,int corner){
        int index=Math.floorMod(corner,4);
        double x=center.getX()+((index&1)==0?-radius:radius+1D);
        double z=center.getZ()+((index&2)==0?-radius:radius+1D);
        double y=center.getY()+halfHeight+1D;
        return new Vec3(x,y,z);
    }

    /** Four edge ticks make the middle of each long domain boundary visible from a distance. */
    public static Vec3 upperEdgeMidpoint(BlockPos center,int radius,int halfHeight,int edge){
        int index=Math.floorMod(edge,4);
        double x=center.getX()+.5D,z=center.getZ()+.5D;
        if(index==0)z=center.getZ()-radius;
        else if(index==1)x=center.getX()+radius+1D;
        else if(index==2)z=center.getZ()+radius+1D;
        else x=center.getX()-radius;
        return new Vec3(x,center.getY()+halfHeight+1D,z);
    }
}
