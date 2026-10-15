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
            return;
        }
        heldKind=null;
        var curio=CuriosCompat.find(mc.player);
        if(curio==null||!(curio.stack().getItem() instanceof ArtifactItem item))return;
        emitCurios(mc,item);
    }
    private static void emitHeld(Minecraft mc,ArtifactItem item){
        var look=mc.player.getLookAngle();var side=look.cross(new Vec3(0,1,0)).normalize();
        var hand=mc.player.getEyePosition().add(look.scale(0.65)).add(side.scale(0.3)).add(0,-0.3,0);
        double angle=mc.player.tickCount*0.12+item.kind.ordinal()+ArtifactTooltips.mode(mc.player.getMainHandItem())*0.22;
        var dust=dust(item.kind,0.3F);
        ParticleOptions accent=accent(item.kind,dust);
        for(int i=0;i<3;i++){
            double orbit=angle+i*Math.PI*2/3,radius=0.055+i*0.022;
            var at=hand.add(side.scale(Math.cos(orbit)*radius)).add(0,Math.sin(orbit)*0.075,0).add(look.scale(Math.sin(orbit)*radius));
            mc.level.addParticle(dust,at.x,at.y,at.z,0,0.008,0);
            if(i==0)mc.level.addParticle(accent,at.x,at.y,at.z,0,0.006,0);
        }
    }
    /** A restrained rotating belt halo makes the Curios passive visible without suggesting an active skill. */
    private static void emitCurios(Minecraft mc,ArtifactItem item){
        var player=mc.player;double phase=player.tickCount*0.075D+item.kind.ordinal()*0.61D;
        var dust=dust(item.kind,item.kind==ArtifactKind.ICARUS?0.34F:0.27F);
        ParticleOptions accent=accent(item.kind,dust);
        double y=player.getY()+Math.min(player.getBbHeight()*.58D,1.05D);
        for(int i=0;i<4;i++){
            double angle=phase+i*Math.PI/2D,radius=.42D+Math.sin(phase+i*.7D)*.035D;
            double x=player.getX()+Math.cos(angle)*radius,z=player.getZ()+Math.sin(angle)*radius;
            mc.level.addParticle(i==0?accent:dust,x,y+Math.sin(angle*2D)*.06D,z,0,.004D,0);
        }
    }
    private static DustParticleOptions dust(ArtifactKind kind,float size){
        int color=kind==ArtifactKind.ICARUS?0xffe8c6:kind.color;
        return new DustParticleOptions(new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),size);
    }
    private static ParticleOptions accent(ArtifactKind kind,ParticleOptions fallback){
        return switch(kind){
            case PALIMPSEST->ParticleTypes.ENCHANT;case CHOIR->ParticleTypes.SCULK_CHARGE_POP;
            case EVENTIDE->ParticleTypes.REVERSE_PORTAL;case CRUCIBLE->ParticleTypes.SMALL_FLAME;
            case INTERREGNUM->ParticleTypes.END_ROD;case WORLDLOOM->ParticleTypes.HAPPY_VILLAGER;
            case ICARUS->ParticleTypes.ENCHANT;case AXIOM->ParticleTypes.SCULK_SOUL;
            case WORLDBREAKER->ParticleTypes.FIREWORK;
            case EXODIUM->ParticleTypes.END_ROD;case IRIDIUM->ParticleTypes.GLOW;case HELLSPEC->ParticleTypes.FLAME;
            default->fallback;
        };
    }
}
