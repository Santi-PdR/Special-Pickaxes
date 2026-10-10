package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ArtifactFeedback {
    private static final Map<UUID,Long> SOUND_READY=new HashMap<>();
    private static final Map<UUID,Long> LISTENER_SOUND_READY=new HashMap<>();
    private ArtifactFeedback() {}
    public static void forget(ServerPlayer player){SOUND_READY.remove(player.getUUID());LISTENER_SOUND_READY.remove(player.getUUID());}
    public static void clearSounds(){SOUND_READY.clear();LISTENER_SOUND_READY.clear();}
    public static void message(ServerPlayer p,String key,Object... values) {
        p.displayClientMessage(Component.translatable("message.specialpickaxes."+key,values),true);
    }
    private static void nearbySound(ServerPlayer p,SoundEvent sound,float volume,float pitch) {
        var level=p.serverLevel();long now=level.getGameTime();var id=p.getUUID();var ready=SOUND_READY.get(id);
        if(ready!=null&&now<ready)return;
        SOUND_READY.put(id,now+ArtifactConfig.SOUND_COOLDOWN.get());
        if(SOUND_READY.size()>1024)SOUND_READY.entrySet().removeIf(entry->entry.getValue()+1200<now);
        if(LISTENER_SOUND_READY.size()>1024)LISTENER_SOUND_READY.entrySet().removeIf(entry->entry.getValue()+1200<now);
        var at=p.getEyePosition().add(p.getLookAngle().scale(2));
        var packet=new ClientboundSoundPacket(Holder.direct(sound),SoundSource.PLAYERS,at.x,at.y,at.z,volume,pitch,level.getRandom().nextLong());
        double range=ArtifactConfig.SOUND_RADIUS.get(),rangeSqr=range*range;
        var nearby=new AABB(at.x-range,at.y-range,at.z-range,at.x+range,at.y+range,at.z+range);
        level.getEntities().get(EntityTypeTest.forClass(ServerPlayer.class),nearby,listener->{
            if(!listener.isAlive()||listener.distanceToSqr(at.x,at.y,at.z)>rangeSqr)
                return AbortableIterationConsumer.Continuation.CONTINUE;
            var listenerId=listener.getUUID();var listenerReady=LISTENER_SOUND_READY.get(listenerId);
            if(listener!=p&&listenerReady!=null&&now<listenerReady)
                return AbortableIterationConsumer.Continuation.CONTINUE;
            LISTENER_SOUND_READY.put(listenerId,now+ArtifactConfig.SOUND_COOLDOWN.get());
            listener.connection.send(packet);
            return AbortableIterationConsumer.Continuation.CONTINUE;
        });
    }
    public static void sound(ServerPlayer p,ArtifactKind kind) {
        sound(p,kind,"activate");
    }
    public static void sound(ServerPlayer p,ArtifactKind kind,String phase) {
        nearbySound(p,kind.sound,phase.equals("alternate")?0.2F:0.25F,0.85F+kind.ordinal()*0.04F);
        RelicEffects.emit(p,kind,phase,p.getEyePosition().add(p.getLookAngle().scale(2)));
    }
    public static void burst(ServerPlayer p,ArtifactKind kind,BlockPos pos,int count) {
        int rgb=kind.color;
        var dust=new DustParticleOptions(new Vector3f(((rgb>>16)&255)/255F,((rgb>>8)&255)/255F,(rgb&255)/255F),1.3F);
        net.minecraft.core.particles.ParticleOptions accent=switch(kind){
            case PALIMPSEST,CHRONICLE->net.minecraft.core.particles.ParticleTypes.ENCHANT;
            case CHOIR,MERIDIAN->net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK;
            case EXODIUM->net.minecraft.core.particles.ParticleTypes.END_ROD;case IRIDIUM->net.minecraft.core.particles.ParticleTypes.GLOW;case HELLSPEC->net.minecraft.core.particles.ParticleTypes.FLAME;
            case EVENTIDE->net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL;
            case CRUCIBLE->net.minecraft.core.particles.ParticleTypes.WAX_ON;
            case INTERREGNUM->net.minecraft.core.particles.ParticleTypes.END_ROD;
            case WORLDLOOM->net.minecraft.core.particles.ParticleTypes.COMPOSTER;
            case KEYSTONE->net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER;
            case ICARUS->net.minecraft.core.particles.ParticleTypes.END_ROD;
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
        p.serverLevel().sendParticles(p,dust,false,pos.getX()+0.5,pos.getY()+0.6,pos.getZ()+0.5,Math.min(8,count),0.25,0.25,0.25,0);
    }
    public static void ring(ServerPlayer p,ArtifactKind kind,BlockPos center,int radius) {
        for(int i=0;i<8;i++) {
            double angle=(i/8.0)*Math.PI*2;
            burst(p,kind,center.offset((int)Math.round(Math.cos(angle)*radius),0,(int)Math.round(Math.sin(angle)*radius)),1);
        }
    }
    /** Compact 3D boundary preview for bounded radial mining; only the activating player receives it. */
    public static void sphere(ServerPlayer p,ArtifactKind kind,BlockPos center,int radius,int innerRadius,int points) {
        int rgb=kind.color;
        var dust=new DustParticleOptions(new Vector3f(((rgb>>16)&255)/255F,((rgb>>8)&255)/255F,(rgb&255)/255F),1.25F);
        boolean hollow=kind==ArtifactKind.AXIOM;
        var outer=hollow?new DustParticleOptions(new Vector3f(.24F,.88F,1F),1.3F):dust;
        var core=hollow?new DustParticleOptions(new Vector3f(.72F,.42F,1F),1.45F):dust;
        int count=Math.max(12,Math.min(hollow?128:64,points));
        for(int shell=0;shell<(innerRadius>0?2:1);shell++){
            double r=shell==0?radius:innerRadius;
            int samples=shell==0?count:Math.max(12,count/2);
            var color=shell==0?outer:core;
            for(int i=0;i<samples;i++){
                double y=1-2*(i+0.5)/samples,ring=Math.sqrt(Math.max(0,1-y*y)),angle=i*2.399963229728653;
                double x=Math.cos(angle)*ring,z=Math.sin(angle)*ring;
                p.serverLevel().sendParticles(p,color,false,center.getX()+0.5+x*r,center.getY()+0.5+y*r,center.getZ()+0.5+z*r,1,0,0,0,0);
            }
        }
    }
    public static void worldloomBore(ServerPlayer p,BlockPos origin,net.minecraft.core.Direction direction){
        var level=p.serverLevel();var green=new DustParticleOptions(new Vector3f(.28F,.9F,.42F),1.25F);
        for(int depth:new int[]{0,8,15})for(int u=-2;u<=2;u++)for(int v=-2;v<=2;v++)if(Math.abs(u)==2||Math.abs(v)==2){
            BlockPos c=origin.relative(direction,depth);BlockPos at=switch(direction.getAxis()){
                case X->c.offset(0,v,u);case Y->c.offset(u,0,v);case Z->c.offset(u,v,0);};
            if(level.hasChunkAt(at))level.sendParticles(p,green,false,at.getX()+.5,at.getY()+.5,at.getZ()+.5,1,0,0,0,0);
        }
    }
    public static void nullWard(ServerPlayer p,int radius){
        var center=p.blockPosition();ring(p,ArtifactKind.AXIOM,center,radius);
        var soul=new DustParticleOptions(new Vector3f(.24F,.88F,1F),1.35F);
        for(int i=0;i<16;i++){
            double angle=i*Math.PI/8;
            p.serverLevel().sendParticles(p,soul,false,p.getX()+Math.cos(angle)*radius,p.getY()+.8,p.getZ()+Math.sin(angle)*radius,1,0,0,0,0);
        }
    }
    /** Bounded ember outlines make Hellspec's core-and-petal quarry legible before it starts. */
    public static void hellBloom(ServerPlayer p,BlockPos center) {
        var ember=new DustParticleOptions(new Vector3f(1F,.24F,.035F),1.35F);
        var heart=new DustParticleOptions(new Vector3f(1F,.72F,.12F),1.5F);
        int[][] offsets={{0,0},{4,0},{-4,0},{0,4},{0,-4}};
        for(int index=0;index<5;index++){
            BlockPos petal=center.offset(offsets[index][0],0,offsets[index][1]);
            var color=index==0?heart:ember;
            for(int i=0;i<16;i++){
                double angle=i*Math.PI/8;
                double x=petal.getX()+.5+Math.cos(angle)*3,z=petal.getZ()+.5+Math.sin(angle)*3;
                p.serverLevel().sendParticles(p,color,false,x,petal.getY()+.12,z,1,0,0,0,0);
            }
            p.serverLevel().sendParticles(p,ParticleTypes.FLAME,false,petal.getX()+.5,petal.getY()+.55,petal.getZ()+.5,3,.15,.12,.15,.015);
        }
    }
    public static void domain(ServerPlayer p,ArtifactKind kind,BlockPos center,int radius,int halfHeight) {
        boolean stasis=kind==ArtifactKind.INTERREGNUM;
        int color=kind.color;var dust=new DustParticleOptions(new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),stasis?1.25F:0.82F);
        var crown=new DustParticleOptions(new Vector3f(0.95F,0.78F,0.42F),stasis?1.05F:0.7F);
        double minX=center.getX()-radius+0.5,maxX=center.getX()+radius+0.5,minZ=center.getZ()-radius+0.5,maxZ=center.getZ()+radius+0.5,y=center.getY()+0.08;
        double topY=center.getY()+halfHeight+0.08;
        // A capped wireframe remains legible while bounding particle packets per field update.
        // Side loops omit corners because the front/back edges already draw them.
        int segments=stasis?Math.max(6,Math.min(8,radius)):Math.max(4,Math.min(6,radius));
        for(int i=0;i<=segments;i++){
            double t=i/(double)segments,x=minX+(maxX-minX)*t,z=minZ+(maxZ-minZ)*t;
            p.serverLevel().sendParticles(p,dust,false,x,y,minZ,1,0,0,0,0);p.serverLevel().sendParticles(p,dust,false,x,y,maxZ,1,0,0,0,0);
            if(i>0&&i<segments){
                p.serverLevel().sendParticles(p,dust,false,minX,y,z,1,0,0,0,0);p.serverLevel().sendParticles(p,dust,false,maxX,y,z,1,0,0,0,0);
            }
            if(i%2==0||i==segments){
                p.serverLevel().sendParticles(p,crown,false,x,topY,minZ,1,0,0,0,0);p.serverLevel().sendParticles(p,crown,false,x,topY,maxZ,1,0,0,0,0);
                if(i>0&&i<segments){
                    p.serverLevel().sendParticles(p,crown,false,minX,topY,z,1,0,0,0,0);p.serverLevel().sendParticles(p,crown,false,maxX,topY,z,1,0,0,0,0);
                }
            }
        }
        for(int dx:new int[]{-radius,radius})for(int dz:new int[]{-radius,radius})for(int i=1;i<=3;i++)
            p.serverLevel().sendParticles(p,i==2?crown:dust,false,center.getX()+dx+0.5,center.getY()-halfHeight+i*(halfHeight*2D/4),center.getZ()+dz+0.5,1,0,0,0,0);
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
            p.serverLevel().sendParticles(p,dust,false,point.x,point.y,point.z,1,0,0,0,0);
        }
    }

    public static void cue(ServerPlayer p,String phase){
        if(!(p.getMainHandItem().getItem() instanceof ArtifactItem item))return;
        float pitch=switch(phase){case "error"->.55F;case "cancel"->.7F;case "complete"->1.25F;case "confirm"->.85F;default->1F;};
        nearbySound(p,item.kind.sound,item.kind==ArtifactKind.WORLDBREAKER?.18F:.16F,pitch+(item.kind.ordinal()%4)*.04F);
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
        if(kind==ArtifactKind.CHRONICLE&&mode==1||kind==ArtifactKind.WORLDBREAKER&&mode==4){
            int count=0;for(var memory:ArtifactState.memories(p,kind)){
                var pos=memory.pos();if(v.contains(pos)&&p.serverLevel().hasChunkAt(pos)&&p.serverLevel().getBlockState(pos).isAir()){
                    p.serverLevel().sendParticles(p,dust,false,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,2,0.2,0.2,0.2,0);if(++count>=24)break;
                }
            }
        }
    }

}
