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

/** Cosmetic only: animated head halo around a familiar vanilla tool. Never sends gameplay packets. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,value=Dist.CLIENT)
public final class RelicAura {
    private static ArtifactKind equipped;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.isPaused()||mc.player.tickCount%4!=0||!(mc.player.getMainHandItem().getItem() instanceof ArtifactItem item))return;
        if(equipped!=item.kind){equipped=item.kind;
            var action=ArtifactInteraction.regional(item.kind,ArtifactTooltips.mode(mc.player.getMainHandItem()))?RelicControl.Action.SELECT:RelicControl.Action.ACTIVATE;
            mc.player.displayClientMessage(mc.player.getMainHandItem().getHoverName().copy().append(" · ").append(RelicKeys.name(action)).append(" → ").append(net.minecraft.network.chat.Component.translatable("key.specialpickaxes."+action.name().toLowerCase(java.util.Locale.ROOT))),true);
        }
        var look=mc.player.getLookAngle();var side=look.cross(new Vec3(0,1,0)).normalize();
        var hand=mc.player.getEyePosition().add(look.scale(0.65)).add(side.scale(0.3)).add(0,-0.3,0);
        double angle=mc.player.tickCount*0.12+item.kind.ordinal();var at=hand.add(side.scale(Math.cos(angle)*0.1)).add(0,Math.sin(angle)*0.1,0);
        int color=item.kind.color;var dust=new DustParticleOptions(new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),0.55F);
        mc.level.addParticle(dust,at.x,at.y,at.z,0,0.006,0);
        if(mc.player.tickCount%12==0){
            var accent=switch(item.kind){
                case EVENTIDE,AXIOM->ParticleTypes.REVERSE_PORTAL;case MERIDIAN,ATLAS,LODESTAR->ParticleTypes.PORTAL;
                case CRUCIBLE,ICARUS,WORLDBREAKER->ParticleTypes.SMALL_FLAME;case INTERREGNUM,AEGIS->ParticleTypes.ELECTRIC_SPARK;
                case CHOIR,SEAM_RIPPER->ParticleTypes.SCRAPE;case COUNTERSEAL->ParticleTypes.SOUL;
                case WORLDLOOM,KEYSTONE,CAUSEWAY->ParticleTypes.WAX_ON;default->ParticleTypes.ENCHANT;};
            mc.level.addParticle(accent,hand.x,hand.y,hand.z,0,0,0);
        }
    }
}
