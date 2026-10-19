package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;

/** Bounded client-side paths used to give passive Curios signatures a distinct silhouette. */
public final class CurioAuraGeometry {
    private CurioAuraGeometry() {}

    /** Four drifting leaves climb in a compact helix around Worldloom's wearer. */
    public static Vec3 worldloomVinePoint(Vec3 origin,double phase,int sample){
        int index=Math.floorMod(sample,4);
        double progress=(index+.5D)/4D;
        double angle=phase+progress*Math.PI*2D;
        double radius=.12D+.16D*progress;
        double rise=.08D+progress*.72D+Math.sin(phase*1.4D+angle)*.035D;
        return origin.add(Math.cos(angle)*radius,rise,Math.sin(angle)*radius);
    }
}
