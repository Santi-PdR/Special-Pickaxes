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
    private static final java.util.Set<String> CHECKED=new java.util.HashSet<>();
    @SubscribeEvent public static void draw(RenderGuiOverlayEvent.Post e){
        if(!Boolean.getBoolean("specialpickaxes.clientSmoke")||e.getOverlay()!=VanillaGuiOverlay.HOTBAR.type())return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||mc.player.tickCount<250)return;
        mc.gui.getChat().clearMessages(false); // Diagnostic capture markers remain in logs, not over the demonstration.
        if(mc.player.getMainHandItem().getItem() instanceof ArtifactItem item){
            boolean shift=net.minecraft.client.gui.screens.Screen.hasShiftDown();String key=item.kind.id+(shift?"-shift":"-normal");
            if(CHECKED.add(key)){
                var lines=mc.player.getMainHandItem().getTooltipLines(mc.player,net.minecraft.world.item.TooltipFlag.Default.NORMAL);
                if(shift?lines.size()<9:lines.size()!=4)throw new IllegalStateException("Tooltip contract mismatch: "+key+" "+lines.size());
                System.out.println("TOOLTIP_VALIDATED "+key+" lines="+lines.size());
            }
            e.getGuiGraphics().renderTooltip(mc.font,mc.player.getMainHandItem(),8,18);
        }
    }
}
