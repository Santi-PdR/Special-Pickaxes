package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;

/** A small bowed arc for Eventide's one-time Curios link to each newly protected ally. */
public final class GraviticBalanceLinkGeometry {
    private GraviticBalanceLinkGeometry() {}

    /** Five interior points rise gently and curl sideways without crossing either player's silhouette. */
    public static Vec3 point(Vec3 start,Vec3 end,int sample){
        int bounded=Math.max(1,Math.min(5,sample));
        double t=bounded/6D;
        Vec3 delta=end.subtract(start);
        double horizontalLength=Math.sqrt(delta.x*delta.x+delta.z*delta.z);
        double sideX=horizontalLength<1.0E-6D?0D:-delta.z/horizontalLength;
        double sideZ=horizontalLength<1.0E-6D?0D:delta.x/horizontalLength;
        double arch=Math.sin(Math.PI*t);
        double curl=Math.sin(Math.PI*2D*t)*.12D;
        return start.lerp(end,t).add(sideX*curl,.12D+.34D*arch,sideZ*curl);
    }
}
