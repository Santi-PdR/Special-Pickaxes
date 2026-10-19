package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ArtifactFeedback {
    public record AuraParticle(ParticleOptions particle,Vec3 position,int count,double dx,double dy,double dz,double speed) {}
    private static final Map<UUID,Long> SOUND_READY=new HashMap<>();
    private static final Map<UUID,Long> LISTENER_SOUND_READY=new HashMap<>();
    private ArtifactFeedback() {}
    public static void forget(ServerPlayer player){SOUND_READY.remove(player.getUUID());LISTENER_SOUND_READY.remove(player.getUUID());}
    public static void clearSounds(){SOUND_READY.clear();LISTENER_SOUND_READY.clear();}
    /** Curios mining flashes reach the wearer and at most three close observers. */
    public static void nearbyMiningPulse(ServerPlayer wearer,BlockPos pos,ParticleOptions primary,int primaryCount,
                                         ParticleOptions accent,int accentCount){
        var level=wearer.serverLevel();double x=pos.getX()+.5D,y=pos.getY()+.58D,z=pos.getZ()+.5D;
        var center=new Vec3(x,y,z);
        emitMiningPulse(wearer,level,center,primary,primaryCount,accent,accentCount);
        var nearby=EntitySelection.nearest(level,ServerPlayer.class,new AABB(x,y,z,x,y,z).inflate(12D),
                viewer->viewer!=wearer&&viewer.isAlive()&&!viewer.isSpectator()&&!viewer.isInvisibleTo(wearer)
                        &&viewer.distanceToSqr(center)<=144D,
                center,3,128);
        for(var viewer:nearby)emitMiningPulse(viewer,level,center,primary,primaryCount,accent,accentCount);
    }
    /** Persistent Curios auras are sent only to the bearer and three visible players within twelve blocks. */
    public static void nearbyWardPulse(ServerPlayer wearer,List<AuraParticle> particles){
        if(particles.isEmpty())return;
        var level=wearer.serverLevel();var audience=nearbyWardAudience(wearer);
        for(var viewer:audience)for(var sample:particles){
            var at=sample.position();
            level.sendParticles(viewer,sample.particle(),false,at.x,at.y,at.z,sample.count(),
                    sample.dx(),sample.dy(),sample.dz(),sample.speed());
        }
    }
    /** Includes the bearer and chooses at most three nearby, non-spectating observers. */
    public static List<ServerPlayer> nearbyWardAudience(ServerPlayer wearer){
        var center=wearer.position();var audience=new java.util.ArrayList<ServerPlayer>(4);audience.add(wearer);
        var nearby=EntitySelection.nearest(wearer.serverLevel(),ServerPlayer.class,wearer.getBoundingBox().inflate(12D),
                viewer->viewer!=wearer&&viewer.isAlive()&&!viewer.isSpectator()&&!viewer.isInvisibleTo(wearer)
                        &&viewer.distanceToSqr(center)<=144D,
                center,3,128);
        audience.addAll(nearby);return List.copyOf(audience);
    }
    /** Tempo tiers recolor the mining cut so Curios' sustained-break bonus is readable without extra ticks. */
    public static void strataTempoPulse(ServerPlayer wearer,BlockPos pos,int streak){
        int tier=Math.max(1,Math.min(3,streak));
        DustParticleOptions cut=switch(tier){
            case 1->new DustParticleOptions(new Vector3f(.32F,.78F,1F),.82F);
            case 2->new DustParticleOptions(new Vector3f(.36F,1F,.78F),.96F);
            default->new DustParticleOptions(new Vector3f(1F,.82F,.42F),1.12F);
        };
        nearbyMiningPulse(wearer,pos,cut,3,tier==3?ParticleTypes.END_ROD:ParticleTypes.GLOW,tier);
    }
    /** A private, one-shot halo marks the transition into the full Strata tempo bonus. */
    public static void strataTempoPeak(ServerPlayer wearer){
        var level=wearer.serverLevel();double x=wearer.getX(),y=wearer.getY()+wearer.getBbHeight()*.52D,z=wearer.getZ();
        double phase=level.getGameTime()*.08D;
        var cyan=new DustParticleOptions(new Vector3f(.35F,.9F,1F),.92F);
        var gold=new DustParticleOptions(new Vector3f(1F,.82F,.42F),1.18F);
        for(int i=0;i<12;i++){
            double progress=i/11D,angle=phase+progress*Math.PI*2D,radius=.34D+progress*.22D;
            level.sendParticles(wearer,(i&1)==0?gold:cyan,false,x+Math.cos(angle)*radius,
                    y-.22D+progress*.58D,z+Math.sin(angle)*radius,1,0,0,0,0);
        }
        level.sendParticles(wearer,ParticleTypes.END_ROD,false,x,y+.36D,z,2,.06D,.08D,.06D,.006D);
        level.sendParticles(wearer,ParticleTypes.GLOW,false,x,y+.12D,z,2,.08D,.12D,.08D,.004D);
    }
    private static void emitMiningPulse(ServerPlayer viewer,net.minecraft.server.level.ServerLevel level,Vec3 center,
                                        ParticleOptions primary,int primaryCount,ParticleOptions accent,int accentCount){
        // A few deliberate points read as a rising cut through the block, while the former
        // random cloud looked identical for Stasis and Strata and cost more particle packets.
        int motes=Math.min(3,Math.max(0,primaryCount));
        double phase=level.getGameTime()*.22D+center.x*.71D+center.z*.39D;
        for(int i=0;i<motes;i++){
            double progress=(i+1D)/(motes+1D);
            double angle=phase+progress*Math.PI*1.4D;
            double radius=.12D+progress*.38D;
            level.sendParticles(viewer,primary,false,center.x+Math.cos(angle)*radius,
                    center.y+progress*.48D,center.z+Math.sin(angle)*radius,1,0,0,0,0);
        }
        for(int i=0;i<Math.min(3,Math.max(0,accentCount));i++){
            double angle=level.getGameTime()*.3D+i*Math.PI*2D/Math.max(1,accentCount);
            double radius=.035D*i;
            level.sendParticles(viewer,accent,false,center.x+Math.cos(angle)*radius,
                    center.y+.42D+i*.025D,center.z+Math.sin(angle)*radius,1,0,0,0,0);
        }
    }
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
        double range=ArtifactConfig.SOUND_RADIUS.get();var origin=p.position();
        var nearby=new AABB(origin.x-range,origin.y-range,origin.z-range,origin.x+range,origin.y+range,origin.z+range);
        level.getEntities().get(EntityTypeTest.forClass(ServerPlayer.class),nearby,listener->{
            if(!listener.isAlive()||!withinSoundRadius(origin,listener.position(),range))
                return AbortableIterationConsumer.Continuation.CONTINUE;
            var listenerId=listener.getUUID();var listenerReady=LISTENER_SOUND_READY.get(listenerId);
            if(listener!=p&&listenerReady!=null&&now<listenerReady)
                return AbortableIterationConsumer.Continuation.CONTINUE;
            LISTENER_SOUND_READY.put(listenerId,now+ArtifactConfig.SOUND_COOLDOWN.get());
            listener.connection.send(packet);
            return AbortableIterationConsumer.Continuation.CONTINUE;
        });
    }
    /** Radius is measured from the activating player, not the forward-offset sound source. */
    public static boolean withinSoundRadius(Vec3 player,Vec3 listener,double radius){
        return radius>=0&&player.distanceToSqr(listener)<=radius*radius;
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
    /** Iridium's ore extraction gets a distinct, private resonance spiral instead of the shared burst. */
    public static void iridiumLodePulse(ServerPlayer p,BlockPos pos){
        var level=p.serverLevel();double phase=level.getGameTime()*.24D;
        var cyan=new DustParticleOptions(new Vector3f(.42F,.94F,1F),1.12F);
        var pearl=new DustParticleOptions(new Vector3f(.9F,1F,1F),.82F);
        double x=pos.getX()+.5D,y=pos.getY()+.52D,z=pos.getZ()+.5D;
        for(int i=0;i<5;i++){
            double progress=i/4D,angle=phase+progress*Math.PI*1.7D,radius=.08D+progress*.29D;
            level.sendParticles(p,(i&1)==0?cyan:pearl,false,x+Math.cos(angle)*radius,
                    y-.2D+progress*.42D,z+Math.sin(angle)*radius,1,0,0,0,0);
        }
        level.sendParticles(p,ParticleTypes.GLOW,false,x,y+.24D,z,1,0,0,0,0);
    }
    /** A short private scar marks the midpoint between adjacent Seam Ripper breaks. */
    public static boolean seamTrail(ServerPlayer p,BlockPos from,BlockPos to) {
        if(from==null||to==null||from.distSqr(to)>5D)return false;
        var point=Vec3.atCenterOf(from).lerp(Vec3.atCenterOf(to),.5D).add(0,.62D,0);
        p.serverLevel().sendParticles(p,ParticleTypes.SCRAPE,false,point.x,point.y,point.z,1,0,0,0,0);
        return true;
    }
    /** At most four adjacent links are sent per mining pulse, to this pick's wearer only. */
    public static int seamTrail(ServerPlayer p,List<BlockPos> points){
        int first=Math.max(1,points.size()-4);
        int sent=0;
        for(int i=first;i<points.size();i++)if(seamTrail(p,points.get(i-1),points.get(i)))sent++;
        return sent;
    }
    /** The Crucible sheds particles of its chosen output, making each conversion legible at a glance. */
    public static void crucibleRephase(ServerPlayer p,BlockPos pos,net.minecraft.world.level.block.state.BlockState material){
        var fragments=new BlockParticleOption(ParticleTypes.BLOCK,material);
        p.serverLevel().sendParticles(p,fragments,false,pos.getX()+.5D,pos.getY()+.58D,pos.getZ()+.5D,5,.18D,.2D,.18D,.055D);
    }
    /** A slow double-height orbit marks the projectile ward while spending fewer particle packets. */
    public static void crucibleWardOrbit(ServerPlayer p,int radius){
        var level=p.serverLevel();double phase=level.getGameTime()*.075D;
        for(int i=0;i<8;i++){
            double angle=phase+i*Math.PI/4D;
            int band=i&1;double r=Math.max(1,radius-band*.8D);
            double x=p.getX()+Math.cos(angle)*r,z=p.getZ()+Math.sin(angle)*r;
            double y=p.getY()+.18D+band*1.05D+Math.sin(angle*2D)*.08D;
            level.sendParticles(p,(i&2)==0?ParticleTypes.SMALL_FLAME:ParticleTypes.WAX_ON,
                    false,x,y,z,1,0,.015D,0,.002D);
        }
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
    /** Eventide's mining charge collapses its field and draws eligible loose drops to its wearer. */
    public static void gravityCollapse(ServerPlayer p,BlockPos center,int drops){
        var level=p.serverLevel();int rgb=ArtifactKind.EVENTIDE.color;
        var core=new DustParticleOptions(new Vector3f(((rgb>>16)&255)/255F,((rgb>>8)&255)/255F,(rgb&255)/255F),1.6F);
        level.sendParticles(p,ParticleTypes.REVERSE_PORTAL,false,center.getX()+.5,center.getY()+.5,center.getZ()+.5,24,1.4,.9,1.4,.12);
        level.sendParticles(p,core,false,center.getX()+.5,center.getY()+.5,center.getZ()+.5,14,.9,.65,.9,.015);
        var eye=p.getEyePosition();
        level.sendParticles(p,ParticleTypes.REVERSE_PORTAL,false,eye.x,eye.y,eye.z,8,.35,.45,.35,.08);
        ring(p,ArtifactKind.EVENTIDE,center,8);
        message(p,"gravity_collapse",drops);
    }
    /** Personal, capped tethers mark Eventide's Curios counter and drop pull without particle spam. */
    public static void gravityTether(ServerPlayer p,Vec3 from,Vec3 to){
        var level=p.serverLevel();
        var violet=new DustParticleOptions(new Vector3f(.66F,.48F,.96F),1.05F);
        for(int i=1;i<=4;i++){
            var point=gravityTetherPoint(from,to,i/5D);
            level.sendParticles(p,violet,false,point.x,point.y,point.z,1,0,0,0,0);
            if((i&1)==0)level.sendParticles(p,ParticleTypes.REVERSE_PORTAL,false,point.x,point.y,point.z,1,0,0,0,0);
        }
        level.sendParticles(p,ParticleTypes.END_ROD,false,to.x,to.y,to.z,1,0,0,0,0);
    }
    /** A short, bounded spiral lets Eventide's pull read as a deliberate gravity path. */
    public static Vec3 gravityTetherPoint(Vec3 from,Vec3 to,double progress){
        double t=Math.max(0D,Math.min(1D,progress));Vec3 delta=to.subtract(from);
        if(delta.lengthSqr()<1.0E-8D)return from;
        Vec3 forward=delta.normalize(),axis=Math.abs(forward.y)<.9D?new Vec3(0,1,0):new Vec3(1,0,0);
        Vec3 side=forward.cross(axis).normalize(),up=forward.cross(side).normalize();
        double radius=Math.sin(Math.PI*t)*.18D,angle=t*Math.PI*2D;
        return from.lerp(to,t).add(side.scale(Math.cos(angle)*radius)).add(up.scale(Math.sin(angle)*radius));
    }
    /** Bounded octagonal previews show the depth and beveled section of Exodium's Starfall Sink. */
    public static void starfallSink(ServerPlayer p,BlockPos origin){
        var level=p.serverLevel();
        var rim=new DustParticleOptions(new Vector3f(1F,.34F,.72F),1.35F);
        var core=new DustParticleOptions(new Vector3f(.96F,.83F,1F),1.15F);
        for(int depth:new int[]{0,8,16,24,31})for(int u=-2;u<=2;u++)for(int v=-2;v<=2;v++){
            if(Math.abs(u)==2&&Math.abs(v)==2)continue;
            if(Math.abs(u)!=2&&Math.abs(v)!=2)continue;
            var at=origin.below(depth).offset(u,0,v);
            if(level.hasChunkAt(at))level.sendParticles(p,rim,false,at.getX()+.5,at.getY()+.5,at.getZ()+.5,1,0,0,0,0);
        }
        for(int depth:new int[]{0,8,16,24,31}){
            var at=origin.below(depth);
            if(level.hasChunkAt(at))level.sendParticles(p,core,false,at.getX()+.5,at.getY()+.5,at.getZ()+.5,1,0,0,0,0);
        }
    }
    /** A short falling-star pulse marks each live Exodium excavation step without broadcasting to distant players. */
    public static void starfallWork(ServerPlayer p,BlockPos at){
        var level=p.serverLevel();double x=at.getX()+.5,y=at.getY()+.65,z=at.getZ()+.5;
        var trail=new DustParticleOptions(new Vector3f(1F,.4F,.78F),1.05F);
        level.sendParticles(p,ParticleTypes.END_ROD,false,x,y,z,1,0,.18,0,.005);
        level.sendParticles(p,trail,false,x,y-.28,z,1,0,0,0,0);
    }
    /** Compact landing burst confirms that Icarian Lift actually reached open sky. */
    public static void icarianSurface(ServerPlayer p){
        var level=p.serverLevel();double x=p.getX(),y=p.getY()+.15D,z=p.getZ();
        var sky=new DustParticleOptions(new Vector3f(.72F,.9F,1F),1.35F);
        for(int i=0;i<12;i++){
            double angle=i*Math.PI/6;
            level.sendParticles(p,sky,false,x+Math.cos(angle)*.85D,y,z+Math.sin(angle)*.85D,1,0,.35D,0,.005D);
        }
        level.sendParticles(p,ParticleTypes.END_ROD,false,x,y+.8D,z,8,.28D,.38D,.28D,.025D);
        level.sendParticles(p,ParticleTypes.CLOUD,false,x,y+.1D,z,5,.35D,.05D,.35D,.015D);
    }
    /** Personal ascent helix: six rising cyan points that never broadcast to distant players. */
    public static void icarianAscent(ServerPlayer p){
        var level=p.serverLevel();double phase=p.tickCount*.22D,x=p.getX(),y=p.getY()+.15D,z=p.getZ();
        var sky=new DustParticleOptions(new Vector3f(.58F,.84F,1F),1.05F);
        for(int i=0;i<6;i++){
            double angle=phase+i*Math.PI/3D;
            int risingPoint=(i+(p.tickCount/2))%6;
            double height=.08D+risingPoint*.12D;
            level.sendParticles(p,sky,false,x+Math.cos(angle)*.48D,y+height,z+Math.sin(angle)*.48D,1,0,.04D,0,0);
        }
        level.sendParticles(p,ParticleTypes.END_ROD,false,x,y+.92D,z,1,.08D,.12D,.08D,.01D);
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
        var level=p.serverLevel();var outline=HellspecBloomShape.boundary();
        int stride=Math.max(1,(outline.size()+191)/192);
        for(int i=0;i<outline.size();i+=stride){
            var offset=outline.get(i);var at=center.offset(offset);
            if(level.hasChunkAt(at))level.sendParticles(p,offset.getY()==0?ember:heart,false,at.getX()+.5,at.getY()+.5,at.getZ()+.5,1,0,0,0,0);
        }
        int[][] cores={{0,0},{4,0},{-4,0},{0,4},{0,-4}};
        for(int[] offset:cores){
            var at=center.offset(offset[0],0,offset[1]);
            if(level.hasChunkAt(at)){
                level.sendParticles(p,heart,false,at.getX()+.5,at.getY()+.4,at.getZ()+.5,2,.08,.1,.08,0);
                level.sendParticles(p,ParticleTypes.FLAME,false,at.getX()+.5,at.getY()+.55,at.getZ()+.5,2,.15,.12,.15,.015);
            }
        }
    }
    /** A bounded advancing fan lets nearby players read the non-mining Hellspec attack. */
    public static void magmaWave(ServerPlayer p,Vec3 forward,double range,double halfWidth){
        var level=p.serverLevel();var right=new Vec3(-forward.z,0,forward.x);var origin=p.position().add(0,.8D,0);
        for(int step=2;step<=range;step+=2){
            double spread=halfWidth*step/range;
            for(int lane=-1;lane<=1;lane++){
                var point=origin.add(forward.scale(step)).add(right.scale(spread*lane));
                if(!level.hasChunkAt(BlockPos.containing(point)))continue;
                level.sendParticles(ParticleTypes.FLAME,point.x,point.y,point.z,2,.08D,.03D,.08D,.006D);
                if(lane==0)level.sendParticles(ParticleTypes.LAVA,point.x,point.y,point.z,1,.04D,.02D,.04D,.002D);
            }
        }
    }
    public static void domain(ServerPlayer p,ArtifactKind kind,BlockPos center,int radius,int halfHeight) {
        if(kind==ArtifactKind.INTERREGNUM){io.github.santipdr.specialpickaxes.network.RelicNetwork.showStasisBoundary(p,center,radius,halfHeight);return;}
        if(kind==ArtifactKind.EVENTIDE){eventideDomain(p,center,radius);return;}
        int color=kind.color;var dust=new DustParticleOptions(new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),0.82F);
        var crown=new DustParticleOptions(new Vector3f(0.95F,0.78F,0.42F),0.7F);
        double minX=center.getX()-radius+0.5,maxX=center.getX()+radius+0.5,minZ=center.getZ()-radius+0.5,maxZ=center.getZ()+radius+0.5,y=center.getY()+0.08;
        double topY=center.getY()+halfHeight+0.08;
        // A capped wireframe remains legible while bounding particle packets per field update.
        // Side loops omit corners because the front/back edges already draw them.
        int segments=Math.max(4,Math.min(6,radius));
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
    /** Three great circles match Eventide's actual spherical influence radius. */
    private static void eventideDomain(ServerPlayer p,BlockPos center,int radius){
        int color=ArtifactKind.EVENTIDE.color;
        var boundary=new DustParticleOptions(new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),.9F);
        boolean repel=(ArtifactState.mode(p,ArtifactKind.EVENTIDE)&1)!=0;
        var flow=repel?new DustParticleOptions(new Vector3f(1F,.48F,.78F),.98F):boundary;
        for(int plane=0;plane<3;plane++)for(int i=0;i<24;i++){
            Vec3 point=eventideSpherePoint(center,radius,plane,Math.PI*2D*i/24D);
            p.serverLevel().sendParticles(p,boundary,false,point.x,point.y,point.z,1,0,0,0,0);
        }
        Vec3 origin=Vec3.atCenterOf(center);
        for(int axis=0;axis<6;axis++){
            Vec3 normal=eventideFlowDirection(0,axis);
            Vec3 point=origin.add(normal.scale(radius)),velocity=eventideFlowDirection(repel?1:0,axis).scale(.12D);
            p.serverLevel().sendParticles(p,flow,false,point.x,point.y,point.z,0,velocity.x,velocity.y,velocity.z,.08D);
        }
    }
    /** Samples one of the field's three great circles. Every point lies exactly on its influence sphere. */
    public static Vec3 eventideSpherePoint(BlockPos center,int radius,int plane,double angle){
        Vec3 origin=Vec3.atCenterOf(center);double x=Math.cos(angle)*radius,y=Math.sin(angle)*radius,z=0D;
        return origin.add(switch(Math.floorMod(plane,3)){case 0->new Vec3(x,y,0);case 1->new Vec3(x,0,y);default->new Vec3(0,x,y);});
    }
    /** Unit radial normal for each principal point on the gravity sphere, reversed for outward polarity. */
    public static Vec3 eventideFlowDirection(int mode,int axis){
        Vec3 inward=switch(Math.floorMod(axis,6)){
            case 0->new Vec3(1,0,0);case 1->new Vec3(-1,0,0);case 2->new Vec3(0,1,0);
            case 3->new Vec3(0,-1,0);case 4->new Vec3(0,0,1);default->new Vec3(0,0,-1);
        };
        return (mode&1)==0?inward:inward.scale(-1D);
    }
    public static Vec3 stasisBoundaryPoint(BlockPos center,int radius,double distance){
        double side=radius*2D,perimeter=side*4D;
        distance=((distance%perimeter)+perimeter)%perimeter;
        double minX=center.getX()-radius+.5D,maxX=center.getX()+radius+.5D;
        double minZ=center.getZ()-radius+.5D,maxZ=center.getZ()+radius+.5D;
        double x,z;
        if(distance<side){x=minX+distance;z=minZ;}
        else if(distance<side*2D){x=maxX;z=minZ+(distance-side);}
        else if(distance<side*3D){x=maxX-(distance-side*2D);z=maxZ;}
        else{x=minX;z=maxZ-(distance-side*3D);}
        return new Vec3(x,center.getY()+.08D,z);
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
