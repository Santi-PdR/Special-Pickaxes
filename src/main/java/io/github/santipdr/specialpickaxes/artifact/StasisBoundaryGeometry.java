package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Pure positions for the small client-rendered markers on the Stasis field's upper corners. */
public final class StasisBoundaryGeometry {
    private StasisBoundaryGeometry() {}

    public static Vec3 upperCorner(BlockPos center,int radius,int halfHeight,int corner){
        int index=Math.floorMod(corner,4);
        double x=center.getX()+((index&1)==0?-radius:radius+1D);
        double z=center.getZ()+((index&2)==0?-radius:radius+1D);
        double y=center.getY()+halfHeight+1D;
        return new Vec3(x,y,z);
    }
}
