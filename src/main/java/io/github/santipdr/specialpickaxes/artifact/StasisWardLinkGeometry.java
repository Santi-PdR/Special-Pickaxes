package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;

/** Geometry for the short Curios Stasis tether between its bearer and a newly protected ally. */
public final class StasisWardLinkGeometry {
    private StasisWardLinkGeometry() {}

    /** Four interior samples keep the tether off both players' silhouettes. */
    public static Vec3 point(Vec3 start,Vec3 end,int sample){
        return start.lerp(end,Math.max(1,Math.min(4,sample))/5D);
    }
}
