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
}
