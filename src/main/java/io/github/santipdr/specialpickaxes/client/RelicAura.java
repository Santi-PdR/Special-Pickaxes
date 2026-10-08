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

/** Cosmetic only: animated head halo around the reference-derived relic. Never sends gameplay packets. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,value=Dist.CLIENT)
public final class RelicAura {
    private static ArtifactKind equipped;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||!(mc.player.getMainHandItem().getItem() instanceof ArtifactItem item)){equipped=null;return;}
        if(mc.isPaused()||mc.options.hideGui||mc.player.tickCount%4!=0)return;
        if(equipped!=item.kind){equipped=item.kind;
            var action=ArtifactInteraction.regional(item.kind,ArtifactTooltips.mode(mc.player.getMainHandItem()))?RelicControl.Action.SELECT:RelicControl.Action.ACTIVATE;
            mc.player.displayClientMessage(mc.player.getMainHandItem().getHoverName().copy().append(" · ").append(RelicKeys.name(action)).append(" → ").append(net.minecraft.network.chat.Component.translatable("key.specialpickaxes."+action.name().toLowerCase(java.util.Locale.ROOT))),true);
        }
        var look=mc.player.getLookAngle();var side=look.cross(new Vec3(0,1,0)).normalize();
        var hand=mc.player.getEyePosition().add(look.scale(0.65)).add(side.scale(0.3)).add(0,-0.3,0);
        double angle=mc.player.tickCount*0.12+item.kind.ordinal()+ArtifactTooltips.mode(mc.player.getMainHandItem())*0.22;
        int color=item.kind.color;var dust=new DustParticleOptions(new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),0.3F);
        ParticleOptions accent=switch(item.kind){
            case PALIMPSEST->ParticleTypes.ENCHANT;case CHOIR->ParticleTypes.SCULK_CHARGE_POP;
            case EVENTIDE->ParticleTypes.REVERSE_PORTAL;case CRUCIBLE->ParticleTypes.SMALL_FLAME;
            case INTERREGNUM->ParticleTypes.END_ROD;case WORLDLOOM->ParticleTypes.HAPPY_VILLAGER;
            case ICARUS->ParticleTypes.CRIT;case AXIOM->ParticleTypes.SCULK_SOUL;
            case WORLDBREAKER->ParticleTypes.FIREWORK;
            default->dust;
        };
        for(int i=0;i<3;i++){
            double orbit=angle+i*Math.PI*2/3,radius=0.055+i*0.022;
            var at=hand.add(side.scale(Math.cos(orbit)*radius)).add(0,Math.sin(orbit)*0.075,0).add(look.scale(Math.sin(orbit)*radius));
            mc.level.addParticle(dust,at.x,at.y,at.z,0,0.008,0);
            if(i==0)mc.level.addParticle(accent,at.x,at.y,at.z,0,0.006,0);
        }
    }
}
