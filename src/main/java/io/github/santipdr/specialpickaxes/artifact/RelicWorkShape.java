package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.phys.Vec3;

/** Active excavation impact glyphs, never ambient emitters. Coordinates are local to the working face. */
public final class RelicWorkShape {
    public static final int WORK_COUNT=8,FINISH_COUNT=16;
    private RelicWorkShape(){}
    public static Vec3 point(AbilityBurstShape.Shape style,int index,boolean complete){
        int count=complete?FINISH_COUNT:WORK_COUNT;
        if(index<0||index>=count)throw new IllegalArgumentException("impact index");
        double t=index/(double)(count-1),a=index*Math.PI*2/count;
        double r=complete?1.2:.38;
        return switch(style){
            case PALIMPSEST -> new Vec3(Math.cos(a)*r,complete?.2+t*.6:.1,Math.sin(a)*r);
            case CHOIR -> new Vec3((t-.5)*r*2,Math.sin(t*Math.PI*3)*r*.35,complete?.25:.05);
            case EVENTIDE -> new Vec3(Math.cos(a)*(1-t*.65)*r,Math.sin(a)*r*.5,Math.sin(a)*(1-t*.65)*r);
            case CRUCIBLE -> new Vec3((index%4-1.5)*r*.6,(index/4-(complete?1.5:.5))*r*.6,.08);
            case INTERREGNUM -> new Vec3(Math.cos(a)*r,Math.sin(a)*r,(index%2)*.12);
            case WORLDLOOM -> new Vec3((index%2==0?-1:1)*r*t,.08,Math.sin(t*Math.PI*2)*r*.3+t*r);
            case ICARUS -> new Vec3((index%2==0?-1:1)*(.1+t*r),t*r*.6,-t*r*.4);
            case AXIOM -> new Vec3(Math.cos(a)*r,Math.sin(a)*r*.5,Math.cos(a*3)*r*.2);
            case WORLDBREAKER -> new Vec3(Math.cos(a)*(index%2==0?r:r*.45),Math.sin(a)*(index%2==0?r:r*.45),.1);
            case EXODIUM -> new Vec3(Math.cos(a)*r*(1-t*.5),r*(1-t),Math.sin(a)*r*(1-t*.5));
            case IRIDIUM -> new Vec3(Math.cos(a)*r,Math.abs(Math.sin(a))*r*.6,Math.sin(a)*r);
            case HELLSPEC -> new Vec3(Math.cos(a)*r*(.7+.3*Math.cos(a*5)),t*r*.8,Math.sin(a)*r*(.7+.3*Math.cos(a*5)));
            case SEAM_RIPPER -> new Vec3((t-.5)*r*2,(index%2==0?-1:1)*r*.22,.05);
        };
    }
}
