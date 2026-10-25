package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

/** Cosmetic-only hand and Curios signatures. Never sends gameplay packets. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,value=Dist.CLIENT)
public final class RelicAura {
    private static ArtifactKind heldKind;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null){heldKind=null;return;}
        if(mc.isPaused()||mc.options.hideGui||mc.player.tickCount%4!=0)return;
        ArtifactItem held=mc.player.getMainHandItem().getItem() instanceof ArtifactItem item?item:null;
        if(held!=null){
            if(heldKind!=held.kind){heldKind=held.kind;
                var action=RelicControl.Action.ACTIVATE;
                mc.player.displayClientMessage(mc.player.getMainHandItem().getHoverName().copy().append(" · ").append(RelicKeys.name(action)).append(" → ").append(net.minecraft.network.chat.Component.translatable("key.specialpickaxes."+action.name().toLowerCase(java.util.Locale.ROOT))),true);
            }
            emitHeld(mc,held);
        } else heldKind=null;
        var curio=CuriosCompat.find(mc.player);
        if(curio==null||!(curio.stack().getItem() instanceof ArtifactItem item))return;
        if(held==null||held.kind!=item.kind)emitCurios(mc,item);
    }
    private static void emitHeld(Minecraft mc,ArtifactItem item){
        var look=mc.player.getLookAngle();var side=look.cross(new Vec3(0,1,0)).normalize();
        var hand=mc.player.getEyePosition().add(look.scale(0.65)).add(side.scale(0.3)).add(0,-0.3,0);
        double angle=mc.player.tickCount*0.12+item.kind.ordinal()+ArtifactTooltips.mode(mc.player.getMainHandItem())*0.22;
        var dust=dust(item.kind,0.3F);
        for(int i=0;i<3;i++){
            double orbit=angle+i*Math.PI*2/3,radius=0.055+i*0.022;
            var at=hand.add(side.scale(Math.cos(orbit)*radius)).add(0,Math.sin(orbit)*0.075,0).add(look.scale(Math.sin(orbit)*radius));
            mc.level.addParticle(dust,at.x,at.y,at.z,0,0.008,0);
        }
    }
    /** A restrained rotating belt halo makes the Curios passive visible without suggesting an active skill. */
    private static void emitCurios(Minecraft mc,ArtifactItem item){
        var player=mc.player;double phase=player.tickCount*0.075D+item.kind.ordinal()*0.61D;
        var dust=dust(item.kind,item.kind==ArtifactKind.ICARUS?0.34F:0.27F);
        double y=player.getY()+Math.min(player.getBbHeight()*.58D,1.05D);
        double yaw=Math.toRadians(player.getYRot());
        var back=new Vec3(Math.sin(yaw),0,-Math.cos(yaw));
        var side=new Vec3(Math.cos(yaw),0,Math.sin(yaw));
        for(int i=0;i<4;i++){
            double angle=phase+i*Math.PI/2D,radius=.42D+Math.sin(phase+i*.7D)*.035D;
            double x=player.getX()+Math.cos(angle)*radius,z=player.getZ()+Math.sin(angle)*radius;
            double particleY=y+Math.sin(angle*2D)*.06D;
            ParticleOptions 
            switch(item.kind){
                case PALIMPSEST -> {
                    angle=phase*.72D+i*Math.PI/2D;radius=.25D+i*.035D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.2D+i*.13D;
                }
                case CHOIR -> {
                    radius=.28D+(.12D+.03D*Math.sin(phase))*(i/2);
                    angle=phase*.18D+i*Math.PI/2D;x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.08D+(i%2)*.18D;
                }
                case EVENTIDE -> {
                    angle=-phase*.55D+i*Math.PI/2D;radius=.34D+(i%2)*.13D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.22D+i*.12D;
                }
                case CRUCIBLE -> {
                    angle=phase*.28D+i*Math.PI/2D;radius=.31D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.12D+(i%2)*.28D;
                }
                case INTERREGNUM -> {
                    double spin=phase*.12D,corner=(i%2==0?-.42D:.42D);
                    double cx=corner,cz=(i<2?-.42D:.42D);
                    x=player.getX()+cx*Math.cos(spin)-cz*Math.sin(spin);
                    z=player.getZ()+cx*Math.sin(spin)+cz*Math.cos(spin);
                    particleY=y+.08D+Math.sin(phase*.3D+i)*.055D;
                }
                case WORLDLOOM -> {
                    angle=phase*.38D+i*Math.PI/2D;radius=.38D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=player.getY()+.12D+(i%2)*.12D;
                }
                case ICARUS -> {
                    int wing=i<2?-1:1;double feather=(i%2)*.22D+.24D;
                    double spread=.20D+(i%2)*.22D;
                    x=player.getX()+back.x*.24D+side.x*wing*spread;
                    z=player.getZ()+back.z*.24D+side.z*wing*spread;
                    particleY=y-.04D+feather+Math.sin(phase+i*.8D)*.035D;
                }
                case AXIOM -> {
                    angle=-phase*.62D+i*Math.PI/2D;radius=.26D+(i%2)*.15D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.12D+(i%2)*.24D;
                }
                case EXODIUM -> {
                    angle=phase*.46D+i*Math.PI/2D;radius=.18D+(i%2)*.25D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.18D+(i%2)*.34D;
                }
                case IRIDIUM -> {
                    angle=phase*.3D+i*Math.PI/2D;radius=.4D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.1D+(i%2)*.2D;
                }
                case WORLDBREAKER -> {
                    angle=phase*.36D+i*Math.PI/2D;radius=.31D+.05D*Math.sin(phase+i);
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.16D+(i%2)*.32D;
                }
                case HELLSPEC -> {
                    angle=phase*.5D+i*Math.PI/2D;radius=.28D+(i%2)*.12D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.16D+(i%2)*.28D;
                }
                case SEAM_RIPPER -> {
                    angle=phase*.22D;radius=.13D+i*.105D;
                    x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
                    particleY=y-.14D+i*.09D;
                }
                default -> { }
            }
            mc.level.addParticle(dust,x,particleY,z,0,.004D,0);
        }
    }
    private static DustParticleOptions dust(ArtifactKind kind,float size){
        int color=kind==ArtifactKind.ICARUS?0xffe8c6:kind.color;
        return new DustParticleOptions(new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),size);
    }

}
