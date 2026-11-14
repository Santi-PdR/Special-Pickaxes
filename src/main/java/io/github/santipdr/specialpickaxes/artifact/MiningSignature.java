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
        var points=new ArrayList<Vec3>();
        for(int i=0;i<16;i++){
            double t=i/15D,a=phase+t*Math.PI*2,x=0,y=0,z=0;
            switch(kind){
                case PALIMPSEST -> {double r=i%2==0?.35:.65;x=Math.cos(a)*r;z=Math.sin(a)*r;}
                case CHOIR -> {x=(t-.5)*1.4;y=Math.sin(t*Math.PI)*.7;z=(i%3)*.08;}
                case EVENTIDE -> {x=Math.cos(a)*t*.7;z=Math.sin(a)*t*.7;y=t*.2;}
                case CRUCIBLE -> {x=(i%4-1.5)*.28;z=(i/4-1.5)*.28;y=(i%2)*.08;}
                case INTERREGNUM -> {x=i<8?(t*2-.5)*.8:0;z=i>=8?((t-.5)*2-.5)*.8:0;y=.12;}
                case WORLDLOOM -> {x=Math.sin(t*Math.PI*4)*.18;z=t*1.4-.7;y=-.5;}
                case ICARUS -> {x=(i%2==0?1:-1)*(.2+t*.6);z=-t*.5;y=t*.3;}
                case AXIOM -> {x=Math.cos(a)*.65;z=Math.sin(a)*.65;y=Math.sin(a*3)*.2;}
                case WORLDBREAKER -> {x=Math.cos(a)*.75;z=Math.sin(a)*.75;y=i%2==0?.25:-.25;}
                case EXODIUM -> {x=Math.cos(a*2)*(.65-t*.3);z=Math.sin(a*2)*(.65-t*.3);y=-t*.6;}
                case IRIDIUM -> {x=Math.cos(a)*.55;z=Math.sin(a)*.55;y=Math.abs(Math.sin(a))*.4;}
                case HELLSPEC -> {x=Math.cos(a*2)*.4;z=Math.sin(a*2)*.4;y=t*.9-.4;}
                case SEAM_RIPPER -> {x=t*1.2-.6;y=(i%4)*.12-.2;z=.2;}
                default -> {continue;}
            }
            points.add(origin.add(x,y,z));
        }
        return List.copyOf(points);
    }
    public static void emit(ServerPlayer player,ArtifactKind kind){
        int rgb=kind.color;
        var dust=new DustParticleOptions(new Vector3f(((rgb>>16)&255)/255F,((rgb>>8)&255)/255F,(rgb&255)/255F),.65F);
        var samples=points(kind,player.position().add(0,.85,0),player.tickCount*.07).stream()
                .map(point->new ArtifactFeedback.AuraParticle(dust,point,1,0,0,0,0)).toList();
        ArtifactFeedback.nearbyWardPulse(player,samples);
    }
}
