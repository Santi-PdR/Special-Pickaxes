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
        RelicEffects.emit(p,kind,"activate",p.getEyePosition().add(p.getLookAngle().scale(2)));
        p.serverLevel().playSound(null,p.blockPosition(),kind.sound,SoundSource.PLAYERS,0.65F,0.85F+kind.ordinal()*0.04F);
    }
    public static void burst(ServerPlayer p,ArtifactKind kind,BlockPos pos,int count) {
        int rgb=kind.color;
        var dust=new DustParticleOptions(new Vector3f(((rgb>>16)&255)/255F,((rgb>>8)&255)/255F,(rgb&255)/255F),1.3F);
        net.minecraft.core.particles.ParticleOptions accent=switch(kind){
            case PALIMPSEST,CHRONICLE->net.minecraft.core.particles.ParticleTypes.ENCHANT;
            case CHOIR,MERIDIAN->net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK;
            case EVENTIDE->net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL;
            case CRUCIBLE->net.minecraft.core.particles.ParticleTypes.WAX_ON;
            case INTERREGNUM->net.minecraft.core.particles.ParticleTypes.END_ROD;
            case WORLDLOOM,KEYSTONE->net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER;
            case ICARUS->net.minecraft.core.particles.ParticleTypes.CRIT;
            case AXIOM->net.minecraft.core.particles.ParticleTypes.SCULK_SOUL;
            case ATLAS,TESSELLATOR->net.minecraft.core.particles.ParticleTypes.PORTAL;
            case WORLDBREAKER->net.minecraft.core.particles.ParticleTypes.GLOW;
            case AEGIS->net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK;
            case LODESTAR->net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL;
            case SEAM_RIPPER->net.minecraft.core.particles.ParticleTypes.SCRAPE;
            case CAUSEWAY->net.minecraft.core.particles.ParticleTypes.WAX_ON;
            case COUNTERSEAL->net.minecraft.core.particles.ParticleTypes.SOUL;
            case COVENANT->net.minecraft.core.particles.ParticleTypes.ENCHANT;};
        if(count>1)p.serverLevel().sendParticles(p,accent,false,pos.getX()+0.5,pos.getY()+0.7,pos.getZ()+0.5,Math.min(3,count),0.15,0.15,0.15,0.02);
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
        if(!(p.getMainHandItem().getItem() instanceof ArtifactItem item))return;
        float pitch=switch(phase){case "error"->.55F;case "cancel"->.7F;case "complete"->1.25F;case "confirm"->.85F;default->1F;};
        p.playNotifySound(item.kind.sound,SoundSource.PLAYERS,item.kind==ArtifactKind.WORLDBREAKER?.7F:.35F,pitch+(item.kind.ordinal()%4)*.04F);
        RelicEffects.emit(p,item.kind,phase,p.getEyePosition().add(p.getLookAngle().scale(2)));
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

    public static void shape(ServerPlayer p,ArtifactKind kind,SelectionVolume v,int mode){
        var dust=new DustParticleOptions(new Vector3f(0.85F,0.95F,1F),1F);
        if(kind==ArtifactKind.KEYSTONE||kind==ArtifactKind.WORLDBREAKER&&mode==1){
            for(int end=0;end<2;end++)for(int i=0;i<24;i++){
                double fraction=i/23D,x,y;
                if(kind==ArtifactKind.KEYSTONE){x=v.min().getX()+fraction*(v.width()-1)+0.5;y=v.min().getY()+Math.round((v.height()-1)*(1-Math.pow(2*fraction-1,2)))+0.5;}
                else {double a=fraction*Math.PI*2;x=v.min().getX()+v.width()*(1+Math.cos(a))/2;y=v.min().getY()+v.height()*(1+Math.sin(a))/2;}
                double z=end==0?v.min().getZ()+0.5:v.max().getZ()+0.5;p.serverLevel().sendParticles(p,dust,false,x,y,z,1,0,0,0,0);
            }
        }
        if(kind==ArtifactKind.PALIMPSEST||kind==ArtifactKind.CHRONICLE&&mode==1||kind==ArtifactKind.WORLDBREAKER&&mode==4){
            int count=0;for(var memory:ArtifactState.memories(p,kind)){
                var pos=memory.pos();if(v.contains(pos)&&p.serverLevel().hasChunkAt(pos)&&p.serverLevel().getBlockState(pos).isAir()){
                    p.serverLevel().sendParticles(p,dust,false,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,2,0.2,0.2,0.2,0);if(++count>=24)break;
                }
            }
        }
    }

}
