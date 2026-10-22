package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Short spatial signatures. A shared global packet budget is independent of gameplay throughput. */
public final class RelicEffects {
    private static final RelicParticleBudget PARTICLE_BUDGET=new RelicParticleBudget(256);
    private RelicEffects(){}
    public static void emit(ServerPlayer p,ArtifactKind k,String phase,Vec3 center){
        int count=k==ArtifactKind.WORLDBREAKER?24:16;
        if(!PARTICLE_BUDGET.tryReserve(p.serverLevel().getServer().getTickCount(),count))return;
        if(k==ArtifactKind.ICARUS){emitIcarianWings(p,phase,center,count);return;}
        ParticleOptions particle=switch(k){
            case PALIMPSEST->ParticleTypes.WAX_OFF;case CHOIR->ParticleTypes.SCULK_CHARGE_POP;
            case EVENTIDE->ParticleTypes.REVERSE_PORTAL;case MERIDIAN->ParticleTypes.PORTAL;
            case CRUCIBLE->ParticleTypes.SMALL_FLAME;case INTERREGNUM->ParticleTypes.END_ROD;
            case WORLDLOOM->ParticleTypes.COMPOSTER;case ICARUS->ParticleTypes.END_ROD;
            case AXIOM->ParticleTypes.SCULK_SOUL;case ATLAS->ParticleTypes.ENCHANT;
            case WORLDBREAKER->phase.equals("fall_guard")?ParticleTypes.END_ROD:ParticleTypes.FIREWORK;case CHRONICLE->ParticleTypes.GLOW;
            case EXODIUM->ParticleTypes.END_ROD;case IRIDIUM->ParticleTypes.GLOW;case HELLSPEC->ParticleTypes.FLAME;
            case KEYSTONE->ParticleTypes.WAX_ON;case TESSELLATOR->ParticleTypes.SCRAPE;
            case AEGIS->ParticleTypes.ELECTRIC_SPARK;case LODESTAR->ParticleTypes.NAUTILUS;
            case SEAM_RIPPER->ParticleTypes.ASH;case CAUSEWAY->ParticleTypes.CLOUD;
            case COUNTERSEAL->ParticleTypes.SOUL;case COVENANT->ParticleTypes.DRIPPING_OBSIDIAN_TEAR;};
        double scale=phase.equals("error")||phase.equals("cancel")?.35:phase.equals("complete")?1.5:phase.equals("alternate")?1.25:1;
        double mode=ArtifactState.mode(p,k)*Math.PI/6+(phase.equals("alternate")?Math.PI/8:0);
        for(int i=0;i<count;i++){
            double t=i/(double)(count-1),a=t*Math.PI*2+mode,x,y,z;
            switch(k){
                case AXIOM->{x=Math.cos(a)*t;z=Math.sin(a)*t;y=t*.5;}
                case EVENTIDE,COUNTERSEAL->{x=Math.cos(a)*(1-t);z=Math.sin(a)*(1-t);y=t*.5;}
                case WORLDLOOM->{x=ArtifactState.mode(p,k)==2?t*2-1:0;y=ArtifactState.mode(p,k)==2?t:0;z=t*2;}
                case KEYSTONE,CAUSEWAY->{x=t*2-1;y=1-x*x;z=(i%2)*.3;}
                case MERIDIAN,ATLAS,TESSELLATOR->{x=(i%2==0?-1:1)*.65;y=Math.sin(a)*.5;z=Math.cos(a)*.5;}
                case ICARUS,SEAM_RIPPER,CHOIR->{x=Math.cos(a*2)*.3;y=Math.sin(a*2)*.3;z=t*2;}
                case PALIMPSEST,COVENANT->{x=(i%3-1)*(1-t);y=t;z=(i%4-1.5)*(1-t);}
                case INTERREGNUM,CHRONICLE,LODESTAR->{x=Math.cos(a);y=Math.sin(a);z=0;}
                case WORLDBREAKER->{if(phase.equals("fall_guard")){x=Math.cos(a)*.52;y=t*1.7;z=Math.sin(a)*.52;}else{x=Math.cos(a)*(1+t);y=Math.sin(a*3)*.4;z=Math.sin(a)*(1+t);}}
                default->{x=Math.cos(a);y=t*.6;z=Math.sin(a);}
            }
            // Give completion a distinct rising helix instead of replaying the activation silhouette.
            // Particle count and global budget stay unchanged; only their positions differ.
            if(phase.equals("complete")){
                double radius=.2D+t*.62D,angle=a+Math.PI/4D;
                x=Math.cos(angle)*radius;y=.12D+t*.95D;z=Math.sin(angle)*radius;
            }
            if(k==ArtifactKind.EVENTIDE){double sign=ArtifactState.mode(p,k)==1?1:-1;p.serverLevel().sendParticles(p,particle,false,center.x+x*scale,center.y+y*scale,center.z+z*scale,0,sign*x,sign*y,sign*z,.08);}
            else p.serverLevel().sendParticles(p,particle,false,center.x+x*scale,center.y+y*scale,center.z+z*scale,1,0,0,0,0);
        }
    }

    /** Paired feather trails give Icarus a wing silhouette while keeping its old 16-particle budget. */
    private static void emitIcarianWings(ServerPlayer player,String phase,Vec3 center,int count){
        var level=player.serverLevel();
        var pearl=new DustParticleOptions(new Vector3f(.78F,.91F,1F),1.02F);
        var gold=new DustParticleOptions(new Vector3f(1F,.78F,.39F),.86F);
        double phaseOffset=phase.equals("alternate")?-.18D:phase.equals("complete")?.22D:0D;
        for(int i=0;i<count;i++){
            Vec3 point=icarianWingPoint(center,i,phaseOffset);
            ParticleOptions feather=(i/2)%3==0?gold:pearl;
            level.sendParticles(player,feather,false,point.x,point.y,point.z,1,0,0,0,0);
        }
        if(phase.equals("complete"))level.sendParticles(player,ParticleTypes.END_ROD,false,
                center.x,center.y+.72D,center.z,2,.035D,.11D,.035D,.004D);
    }

    /** Sample geometry for the paired, swept-back Icarus wing trails. */
    public static Vec3 icarianWingPoint(Vec3 center,int sample,double phaseOffset){
        int index=Math.floorMod(sample,16);int feather=index/2;
        double side=(index&1)==0?-1D:1D,progress=feather/7D;
        double spread=.16D+progress*.78D;
        double lift=.12D+Math.sin(progress*Math.PI)*.34D;
        double sweep=.42D-progress*.84D+phaseOffset;
        return center.add(side*spread,lift,sweep);
    }
}
