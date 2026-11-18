package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;

/** Thirteen authored, bounded cast animations. No world reads or ambient emitter. */
public enum RelicParticleStyle {
    PALIMPSEST(0xffd783,30,.34F,.035F), CHOIR(0x46ede0,28,.30F,-.05F),
    EVENTIDE(0xa67aff,36,.40F,.08F), CRUCIBLE(0xff8648,32,.32F,-.09F),
    INTERREGNUM(0x8ae5ff,34,.36F,.015F), WORLDLOOM(0x64ffa9,32,.36F,-.02F),
    ICARUS(0xffc057,28,.40F,.04F), AXIOM(0xd9ffbd,30,.38F,-.06F),
    WORLDBREAKER(0xf1bf65,26,.44F,.025F), EXODIUM(0xff6ba5,24,.40F,-.04F),
    IRIDIUM(0x74d7d0,34,.32F,.06F), HELLSPEC(0xf35a39,28,.44F,-.07F),
    SEAM_RIPPER(0xf5ac79,24,.36F,.01F);
    public final int rgb,lifetime;
    public final float size,spin;
    RelicParticleStyle(int rgb,int lifetime,float size,float spin){this.rgb=rgb;this.lifetime=lifetime;this.size=size;this.spin=spin;}
    /** Secondary spells have their own motion, not merely a faster primary animation. */
    public Vec3 alternateDisplacement(double progress,double phase){
        double t=Double.isFinite(progress)?Math.max(0,Math.min(1,progress)):0;
        double a=Double.isFinite(phase)?phase:0,c=Math.cos(a),s=Math.sin(a);
        return switch(this){
            case PALIMPSEST -> new Vec3(c*(1-t)*.5,t*.55,s*(1-t)*.5);
            case CHOIR -> new Vec3(Math.sin(t*12)*.12,Math.cos(t*12)*.12,t*.85);
            case EVENTIDE -> new Vec3(c*(1-t)*.6,Math.sin(t*Math.PI)*.3,s*(1-t)*.6);
            case CRUCIBLE -> new Vec3(c*t*.7,Math.sin(t*Math.PI*2)*.12,s*t*.7);
            case INTERREGNUM -> new Vec3(c*t*.7,Math.floor(t*6)*.04,s*t*.7);
            case WORLDLOOM -> new Vec3(Math.sin(a+t*7)*.18,t*.18,t*.9);
            case ICARUS -> new Vec3(c*t*.2,t*.95,s*t*.2);
            case AXIOM -> new Vec3(c*(1-t)*.65,-t*.4,s*(1-t)*.65);
            case WORLDBREAKER -> new Vec3(Math.cos(a+t*7)*t*.7,t*.3,Math.sin(a+t*7)*t*.7);
            case EXODIUM -> new Vec3(c*(1-t)*.5,-t*.85,s*(1-t)*.5);
            case IRIDIUM -> new Vec3(c*(1-t)*.75,t*.3,s*(1-t)*.75);
            case HELLSPEC -> new Vec3(c*t*.65,t*t*.7,s*t*.65);
            case SEAM_RIPPER -> new Vec3(c*t*.85,t*.12,s*t*.85);
        };
    }
    public Vec3 displacement(double progress,double phase){
        double t=Double.isFinite(progress)?Math.max(0,Math.min(1,progress)):0;
        double a=Double.isFinite(phase)?phase:0,c=Math.cos(a),s=Math.sin(a);
        return switch(this){
            case PALIMPSEST -> new Vec3(c*t*.45,Math.sin(t*Math.PI)*.45,s*t*.45);
            case CHOIR -> new Vec3(c*Math.sin(t*Math.PI*3)*.18,t*.25,s*t*.65);
            case EVENTIDE -> new Vec3(Math.cos(a+t*5)*t*.65,t*.12,Math.sin(a+t*5)*t*.65);
            case CRUCIBLE -> new Vec3(Math.cos(a-t*6)*.4*t,t*.65,Math.sin(a-t*6)*.4*t);
            case INTERREGNUM -> new Vec3(c*Math.sin(t*Math.PI)*.12,Math.floor(t*4)*.06,s*Math.sin(t*Math.PI)*.12);
            case WORLDLOOM -> new Vec3(c*t*.3,t*t*.65,s*t*.3);
            case ICARUS -> new Vec3(c*Math.sin(t*Math.PI)*.6,t*.7,s*t*.18);
            case AXIOM -> new Vec3(-c*t*.5,Math.sin(t*Math.PI)*.1,-s*t*.5);
            case WORLDBREAKER -> new Vec3(c*t*.7,4*t*(1-t)*.3,s*t*.7);
            case EXODIUM -> new Vec3(c*t*.18,-t*.7,s*t*.18);
            case IRIDIUM -> new Vec3(Math.cos(a+t*3)*.25*t,t*.5,Math.sin(a+t*3)*.25*t);
            case HELLSPEC -> new Vec3(c*t*.45,Math.sin(t*Math.PI)*.7,s*t*.45);
            case SEAM_RIPPER -> new Vec3(c*t*.8,Math.sin(t*8)*.06,s*t*.08);
        };
    }
}
