package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.joml.Vector3f;

public final class ArtifactFeedback {
    private ArtifactFeedback() {}
    public static void message(ServerPlayer p,String key,Object... values) {
        p.displayClientMessage(Component.translatable("message.specialpickaxes."+key,values),true);
    }
    public static void sound(ServerPlayer p,ArtifactKind kind) {
        p.serverLevel().playSound(null,p.blockPosition(),kind.sound,SoundSource.PLAYERS,0.65F,0.85F+kind.ordinal()*0.04F);
    }
    public static void burst(ServerPlayer p,ArtifactKind kind,BlockPos pos,int count) {
        int rgb=kind.color;
        var dust=new DustParticleOptions(new Vector3f(((rgb>>16)&255)/255F,((rgb>>8)&255)/255F,(rgb&255)/255F),1.3F);
        p.serverLevel().sendParticles(dust,pos.getX()+0.5,pos.getY()+0.6,pos.getZ()+0.5,Math.min(8,count),0.25,0.25,0.25,0);
    }
    public static void ring(ServerPlayer p,ArtifactKind kind,BlockPos center,int radius) {
        for(int i=0;i<8;i++) {
            double angle=(i/8.0)*Math.PI*2;
            burst(p,kind,center.offset((int)Math.round(Math.cos(angle)*radius),0,(int)Math.round(Math.sin(angle)*radius)),1);
        }
    }
    public static void preview(ServerPlayer p,ArtifactKind kind,java.util.List<WorkStep> steps) {
        int stride=Math.max(1,steps.size()/24);
        for(int i=0,count=0;i<steps.size() && count<24;i+=stride,count++) {
            var pos=steps.get(i).pos();
            if(p.serverLevel().hasChunkAt(pos)) burst(p,kind,pos,1);
        }
    }
    public static void trace(ServerPlayer p,ArtifactKind kind,BlockPos a,BlockPos b) {
        var start=net.minecraft.world.phys.Vec3.atCenterOf(a);var end=net.minecraft.world.phys.Vec3.atCenterOf(b);
        for(int i=0;i<=16;i++) {
            var point=start.lerp(end,i/16.0);if(!p.serverLevel().hasChunkAt(BlockPos.containing(point))) continue;
            int color=i%2==0?kind.color:0xffd783;
            var dust=new DustParticleOptions(new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),1.3F);
            p.serverLevel().sendParticles(dust,point.x,point.y,point.z,1,0,0,0,0);
        }
    }

    public static void cue(ServerPlayer p,String phase){
        var sound=switch(phase){case "error"->net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BASS.value();case "cancel"->net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH;case "complete"->net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP;case "confirm"->net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT;default->net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value();};
        p.playNotifySound(sound,SoundSource.PLAYERS,0.45F,phase.equals("error")?0.6F:1.2F);
    }
    public static void box(ServerPlayer p,ArtifactKind kind,SelectionVolume v,boolean target){
        int rgb=target?0xffd783:kind.color;var dust=new DustParticleOptions(new Vector3f(((rgb>>16)&255)/255F,((rgb>>8)&255)/255F,(rgb&255)/255F),0.8F);
        // Fixed packet budget independent of volume. Only the selecting player receives wireframe particles.
        for(int axis=0;axis<3;axis++)for(int a=0;a<2;a++)for(int b=0;b<2;b++)for(int j=0;j<=6;j++){
            double[] low={v.min().getX(),v.min().getY(),v.min().getZ()},size={v.width(),v.height(),v.depth()},q=low.clone();
            q[axis]+=size[axis]*j/6;q[(axis+1)%3]+=size[(axis+1)%3]*a;q[(axis+2)%3]+=size[(axis+2)%3]*b;
            p.serverLevel().sendParticles(p,dust,false,q[0],q[1],q[2],1,0,0,0,0);
        }
    }

}
