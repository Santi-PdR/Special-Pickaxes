package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;

/** Finite, bounded item-only attraction. Malformed velocities from other mods cannot escape the cap. */
public final class UtilityPullMotion {
    private UtilityPullMotion(){}
    public static Vec3 velocity(Vec3 delta,Vec3 previous,double inertia,double strength,double cap){
        if(!Double.isFinite(inertia)||inertia<0||inertia>1||!Double.isFinite(strength)||strength<0
                ||!Double.isFinite(cap)||cap<=0)throw new IllegalArgumentException("Invalid pull profile");
        double distance=length(delta);
        if(!Double.isFinite(distance)||distance<1e-8)return Vec3.ZERO;
        Vec3 inherited=finite(previous)?bounded(previous,cap):Vec3.ZERO;
        Vec3 direction=new Vec3(delta.x/distance,delta.y/distance,delta.z/distance);
        return bounded(inherited.scale(inertia).add(direction.scale(Math.min(strength,cap))),cap);
    }
    private static Vec3 bounded(Vec3 vector,double cap){
        double length=length(vector);
        if(!Double.isFinite(length))return Vec3.ZERO;
        return length<=cap?vector:new Vec3(vector.x/length*cap,vector.y/length*cap,vector.z/length*cap);
    }
    private static double length(Vec3 v){return Math.hypot(Math.hypot(v.x,v.y),v.z);}
    private static boolean finite(Vec3 v){return Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z);}
}
