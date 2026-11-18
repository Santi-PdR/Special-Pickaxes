package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;

/** Pure, deterministic cast silhouettes. No tick state, random allocation or world access. */
public final class AbilityBurstShape {
    public enum Shape { PALIMPSEST, CHOIR, EVENTIDE, CRUCIBLE, INTERREGNUM, WORLDLOOM, ICARUS, AXIOM, WORLDBREAKER, EXODIUM, IRIDIUM, HELLSPEC, SEAM_RIPPER }
    public static final int COUNT=16;
    private AbilityBurstShape(){}

    /** Local coordinates: x=right, y=up, z=forward. Every burst stays within two blocks. */
    public static Vec3 offset(Shape kind,int index){
        if(index<0||index>=COUNT)throw new IllegalArgumentException("burst index");
        double t=index/(double)(COUNT-1),angle=index*Math.PI/8;
        double radius=.35+(index%2)*.2;
        return switch(kind){
            case ICARUS -> new Vec3((index%2==0?-1:1)*(.15+.75*t),.15+.65*t,.35*t);
            case IRIDIUM -> new Vec3(Math.cos(angle)*(.9-.5*t),.1+.3*t,Math.sin(angle)*(.9-.5*t));
            case SEAM_RIPPER -> new Vec3((index%4-1.5)*.3,(index/4)*.25,0);
            case CHOIR -> new Vec3(Math.cos(angle)*.5,Math.sin(angle)*.5,.8*t);
            case EXODIUM -> new Vec3((index%2==0?-1:1)*.6*(1-t),1.2*(1-t),.65*t);
            case CRUCIBLE -> new Vec3(Math.cos(angle)*radius,.9*t,Math.sin(angle)*radius);
            case WORLDLOOM -> new Vec3((index%2==0?-1:1)*.3,.08,.1+.9*t);
            case AXIOM -> new Vec3(Math.cos(angle)*.7,Math.sin(angle)*.7,0);
            case PALIMPSEST -> new Vec3(Math.cos(angle)*radius,.25*Math.sin(2*angle),Math.sin(angle)*radius);
            case HELLSPEC -> new Vec3(Math.cos(angle)*(.55+.2*Math.cos(angle*5)),.3*Math.sin(angle*5),Math.sin(angle)*(.55+.2*Math.cos(angle*5)));
            case EVENTIDE -> new Vec3(Math.cos(angle)*(.85-.6*t),Math.sin(angle*2)*.15,Math.sin(angle)*(.85-.6*t));
            case INTERREGNUM -> new Vec3((index%4-1.5)*.3,(index/4-1.5)*.3,.12*Math.sin(angle));
            case WORLDBREAKER -> new Vec3((index%2==0?-1:1)*t*.8,.1+Math.abs(Math.sin(angle))*.3,t*.7);
        };
    }

    /** A yaw-only frame stays finite when the caster looks straight up or down. */
    public static Vec3 orient(Vec3 offset,Vec3 look){
        Vec3 forward=new Vec3(look.x,0,look.z);
        forward=forward.lengthSqr()<1.0E-8?new Vec3(0,0,1):forward.normalize();
        Vec3 right=new Vec3(forward.z,0,-forward.x);
        return right.scale(offset.x).add(0,offset.y,0).add(forward.scale(offset.z));
    }
}
