package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Pure positions for the small client-rendered markers on the Stasis field's upper edge. */
public final class StasisBoundaryGeometry {
    private StasisBoundaryGeometry() {}

    /** A moving marker follows the square perimeter at the domain's actual upper plane. */
    public static Vec3 upperPerimeterPoint(BlockPos center,int radius,int halfHeight,double distance){
        return perimeterPoint(center,radius,distance,center.getY()+halfHeight+1D);
    }

    public static Vec3 perimeterPoint(BlockPos center,int radius,double distance,double y){
        double side=radius*2D,perimeter=side*4D;
        distance=((distance%perimeter)+perimeter)%perimeter;
        double minX=center.getX()-radius+.5D,maxX=center.getX()+radius+.5D;
        double minZ=center.getZ()-radius+.5D,maxZ=center.getZ()+radius+.5D;
        double x,z;
        if(distance<side){x=minX+distance;z=minZ;}
        else if(distance<side*2D){x=maxX;z=minZ+(distance-side);}
        else if(distance<side*3D){x=maxX-(distance-side*2D);z=maxZ;}
        else{x=minX;z=maxZ-(distance-side*3D);}
        return new Vec3(x,y,z);
    }

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
