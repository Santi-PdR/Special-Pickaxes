package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.ArtifactItem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Loaded exclusively on the physical client; all displayed values originate on the server. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,value=Dist.CLIENT)
public final class ArtifactHud {
    private ArtifactHud() {}
    @SubscribeEvent public static void render(RenderGuiOverlayEvent.Post event) {
        if(event.getOverlay()!=VanillaGuiOverlay.HOTBAR.type()) return;
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.options.hideGui) return;
        var stack=mc.player.getMainHandItem();
        if(!(stack.getItem() instanceof ArtifactItem item)) return;
        var tag=stack.getTag();int charge=tag==null?0:Math.min(256,Math.max(0,tag.getInt("artifactCharge")));
        int mode=tag==null?1:tag.getInt("artifactMode")+1;int work=tag==null?0:tag.getInt("artifactWork");
        int x=mc.getWindow().getGuiScaledWidth()/2-80,y=mc.getWindow().getGuiScaledHeight()-76;
        var gui=event.getGuiGraphics();gui.fill(x,y,x+160,y+4,0x990a1020);
        gui.fill(x,y,x+charge*160/256,y+4,0xff000000|item.kind.color);
        gui.drawString(mc.font,Component.translatable("ux.specialpickaxes.mode",Component.translatable("mode.specialpickaxes."+io.github.santipdr.specialpickaxes.artifact.ArtifactInteraction.modeKey(item.kind,mode-1))),x,y-11,item.kind.color,true);
        if(tag!=null)gui.drawString(mc.font,Component.translatable("status.specialpickaxes."+tag.getString("artifactStatus")),x,y+7,0xc5d6e7,true);
    }
}
