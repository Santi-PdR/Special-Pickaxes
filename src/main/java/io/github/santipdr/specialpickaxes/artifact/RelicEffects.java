package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Short spatial signatures. A shared global packet budget is independent of gameplay throughput. */
public final class RelicEffects {
    private static long tick=Long.MIN_VALUE;private static int remaining;
    private RelicEffects(){}
    public static void emit(ServerPlayer p,ArtifactKind k,String phase,Vec3 center){
        long now=ArtifactState.now(p);if(tick!=now){tick=now;remaining=256;}
        int count=k==ArtifactKind.WORLDBREAKER?24:12;if(remaining<count)return;remaining-=count;
        ParticleOptions particle=switch(k){
            case PALIMPSEST->ParticleTypes.WAX_OFF;case CHOIR->ParticleTypes.SCULK_CHARGE_POP;
            case EVENTIDE->ParticleTypes.REVERSE_PORTAL;case MERIDIAN->ParticleTypes.PORTAL;
            case CRUCIBLE->ParticleTypes.SMALL_FLAME;case INTERREGNUM->ParticleTypes.END_ROD;
            case WORLDLOOM->ParticleTypes.COMPOSTER;case ICARUS->ParticleTypes.CRIT;
            case AXIOM->ParticleTypes.SCULK_SOUL;case ATLAS->ParticleTypes.ENCHANT;
            case WORLDBREAKER->ParticleTypes.FIREWORK;case CHRONICLE->ParticleTypes.GLOW;
            case KEYSTONE->ParticleTypes.WAX_ON;case TESSELLATOR->ParticleTypes.SCRAPE;
            case AEGIS->ParticleTypes.ELECTRIC_SPARK;case LODESTAR->ParticleTypes.NAUTILUS;
            case SEAM_RIPPER->ParticleTypes.ASH;case CAUSEWAY->ParticleTypes.CLOUD;
            case COUNTERSEAL->ParticleTypes.SOUL;case COVENANT->ParticleTypes.DRIPPING_OBSIDIAN_TEAR;};
        double scale=phase.equals("error")||phase.equals("cancel")?.35:phase.equals("complete")?1.5:1;
        double mode=ArtifactState.mode(p,k)*Math.PI/6;
        for(int i=0;i<count;i++){
            double t=i/(double)(count-1),a=t*Math.PI*2+mode,x,y,z;
            switch(k){
                case EVENTIDE,AXIOM,COUNTERSEAL->{x=Math.cos(a)*(1-t);z=Math.sin(a)*(1-t);y=t*.5;}
                case WORLDLOOM,KEYSTONE,CAUSEWAY->{x=t*2-1;y=1-x*x;z=(i%2)*.3;}
                case MERIDIAN,ATLAS,TESSELLATOR->{x=(i%2==0?-1:1)*.65;y=Math.sin(a)*.5;z=Math.cos(a)*.5;}
                case ICARUS,SEAM_RIPPER,CHOIR->{x=Math.cos(a*2)*.3;y=Math.sin(a*2)*.3;z=t*2;}
                case PALIMPSEST,COVENANT->{x=(i%3-1)*(1-t);y=t;z=(i%4-1.5)*(1-t);}
                case INTERREGNUM,CHRONICLE,LODESTAR->{x=Math.cos(a);y=Math.sin(a);z=0;}
                case WORLDBREAKER->{x=Math.cos(a)*(1+t);y=Math.sin(a*3)*.4;z=Math.sin(a)*(1+t);}
                default->{x=Math.cos(a);y=t*.6;z=Math.sin(a);}
            }
            if(k==ArtifactKind.EVENTIDE){double sign=ArtifactState.mode(p,k)==1?1:-1;p.serverLevel().sendParticles(p,particle,false,center.x+x*scale,center.y+y*scale,center.z+z*scale,0,sign*x,sign*y,sign*z,.08);}
            else p.serverLevel().sendParticles(p,particle,false,center.x+x*scale,center.y+y*scale,center.z+z*scale,1,0,0,0,0);
        }
    }
}
