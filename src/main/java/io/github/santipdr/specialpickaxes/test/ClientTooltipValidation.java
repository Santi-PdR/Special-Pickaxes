package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.ArtifactItem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Real tooltip rendering in the CI gallery, not a fabricated image. Excluded from release. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,value=Dist.CLIENT)
public final class ClientTooltipValidation {
    @SubscribeEvent public static void draw(RenderGuiOverlayEvent.Post e){
        if(!Boolean.getBoolean("specialpickaxes.clientSmoke")||e.getOverlay()!=VanillaGuiOverlay.HOTBAR.type())return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||mc.player.tickCount<250)return;
        if(mc.player.getMainHandItem().getItem() instanceof ArtifactItem)e.getGuiGraphics().renderTooltip(mc.font,mc.player.getMainHandItem(),8,18);
    }
}
