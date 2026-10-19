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

    /** Eight mirrored feather points define Icarus' compact wing silhouette. */
    public static Vec3 icarusFeatherPoint(Vec3 origin,double yawRadians,double phase,int sample){
        return icarusFeatherPoint(origin,yawRadians,phase,sample,true);
    }

    /** The wings fold at rest and spread into a stronger wind-catching flap while soaring. */
    public static Vec3 icarusFeatherPoint(Vec3 origin,double yawRadians,double phase,int sample,boolean soaring){
        int index=Math.floorMod(sample,8),feather=index%4,wing=index<4?-1:1;
        double spread=soaring?1.45D:.72D;
        double span=(.2D+feather*.15D)*spread;
        double trail=.12D+feather*.07D+(soaring?feather*.035D:0D);
        double flap=Math.sin(phase+feather*.42D)*(soaring?.09D+feather*.035D:.022D+feather*.01D);
        Vec3 back=new Vec3(Math.sin(yawRadians),0,-Math.cos(yawRadians));
        Vec3 side=new Vec3(Math.cos(yawRadians),0,Math.sin(yawRadians));
        return origin.add(back.scale(trail)).add(side.scale(wing*span))
                .add(0,.08D+(3-feather)*.025D+flap,0);
    }
}
