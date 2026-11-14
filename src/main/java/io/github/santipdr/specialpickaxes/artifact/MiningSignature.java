package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

/** Thirteen compact tool signatures, not combat explosions. All share the global aura budget. */
public final class MiningSignature {
    private MiningSignature(){}
    public static List<Vec3> points(ArtifactKind kind,Vec3 origin,double phase){
        int style=switch(kind){
            case PALIMPSEST->0;case CHOIR->1;case EVENTIDE->2;case CRUCIBLE->3;case INTERREGNUM->4;
            case WORLDLOOM->5;case ICARUS->6;case AXIOM->7;case WORLDBREAKER->8;case EXODIUM->9;
            case IRIDIUM->10;case HELLSPEC->11;case SEAM_RIPPER->12;default->-1;
        };
        return points(style,origin,phase);
    }
    /** Pure geometry does not bootstrap Forge sound/item registries in unit tests. */
    public static List<Vec3> points(int style,Vec3 origin,double phase){
        phase=Double.isFinite(phase)?phase:0D;
        var points=new ArrayList<Vec3>();
        for(int i=0;i<16;i++){
            double t=i/15D,a=phase+t*Math.PI*2,x=0,y=0,z=0;
            switch(style){
                case 0 -> {double r=i%2==0?.35:.65;x=Math.cos(a)*r;z=Math.sin(a)*r;}
                case 1 -> {x=(t-.5)*1.4;y=Math.sin(t*Math.PI)*.7;z=(i%3)*.08;}
                case 2 -> {x=Math.cos(a)*t*.7;z=Math.sin(a)*t*.7;y=t*.2;}
                case 3 -> {x=(i%4-1.5)*.28;z=(i/4-1.5)*.28;y=(i%2)*.08;}
                case 4 -> {x=i<8?(t*2-.5)*.8:0;z=i>=8?((t-.5)*2-.5)*.8:0;y=.12;}
                case 5 -> {x=Math.sin(t*Math.PI*4)*.18;z=t*1.4-.7;y=-.5;}
                case 6 -> {x=(i%2==0?1:-1)*(.2+t*.6);z=-t*.5;y=t*.3;}
                case 7 -> {x=Math.cos(a)*.65;z=Math.sin(a)*.65;y=Math.sin(a*3)*.2;}
                case 8 -> {x=Math.cos(a)*.75;z=Math.sin(a)*.75;y=i%2==0?.25:-.25;}
                case 9 -> {x=Math.cos(a*2)*(.65-t*.3);z=Math.sin(a*2)*(.65-t*.3);y=-t*.6;}
                case 10 -> {x=Math.cos(a)*.55;z=Math.sin(a)*.55;y=Math.abs(Math.sin(a))*.4;}
                case 11 -> {x=Math.cos(a*2)*.4;z=Math.sin(a*2)*.4;y=t*.9-.4;}
                case 12 -> {x=t*1.2-.6;y=(i%4)*.12-.2;z=.2;}
                default -> {continue;}
            }
            points.add(origin.add(x,y,z));
        }
        return List.copyOf(points);
    }
    /** Maps a local signature to the mining direction, preserving size even when looking vertically. */
    public static Vec3 orient(Vec3 local,Vec3 look){
        double square=look.x*look.x+look.z*look.z;
        Vec3 forward=Double.isFinite(square)&&square>1e-8?new Vec3(look.x,0,look.z).normalize():new Vec3(0,0,1);
        Vec3 right=new Vec3(-forward.z,0,forward.x);
        return right.scale(local.x).add(0,local.y,0).add(forward.scale(local.z));
    }
    public static void emit(ServerPlayer player,ArtifactKind kind){
        int rgb=kind.color;
        var dust=new DustParticleOptions(new Vector3f(((rgb>>16)&255)/255F,((rgb>>8)&255)/255F,(rgb&255)/255F),.65F);
        var look=player.getLookAngle();var origin=player.position().add(0,.65,0).add(orient(new Vec3(0,0,.75),look));
        var samples=points(kind,Vec3.ZERO,player.tickCount*.07).stream()
                .map(local->new ArtifactFeedback.AuraParticle(dust,origin.add(orient(local,look)),1,0,0,0,0)).toList();
        ArtifactFeedback.nearbyWardPulse(player,samples);
    }
}
