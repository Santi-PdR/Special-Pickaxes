package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;

/** Bounded client-side paths used to give passive Curios signatures a distinct silhouette. */
public final class CurioAuraGeometry {
    private CurioAuraGeometry() {}

    /** Five bounded samples bow a one-time passive link above the straight path between allies. */
    public static Vec3 sharedAllyLinkPoint(Vec3 start,Vec3 end,int sample,double lift){
        int bounded=Math.max(1,Math.min(5,sample));
        double t=bounded/6D;
        Vec3 delta=end.subtract(start);
        double horizontalLength=Math.sqrt(delta.x*delta.x+delta.z*delta.z);
        double sideX=horizontalLength<1.0E-6D?0D:-delta.z/horizontalLength;
        double sideZ=horizontalLength<1.0E-6D?0D:delta.x/horizontalLength;
        double arch=Math.sin(Math.PI*t);
        double curl=Math.sin(Math.PI*2D*t)*.12D;
        return start.lerp(end,t).add(sideX*curl,.12D+Math.max(0D,Math.min(.5D,lift))*arch,sideZ*curl);
    }

    /** Stable horizontal hand-orbit axis, including when the wearer aims straight up or down. */
    public static Vec3 handSide(Vec3 look){
        double horizontalSqr=look.x*look.x+look.z*look.z;
        if(horizontalSqr<1.0E-8D)return new Vec3(1D,0D,0D);
        double inverseLength=1D/Math.sqrt(horizontalSqr);
        return new Vec3(-look.z*inverseLength,0D,look.x*inverseLength);
    }

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
