package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.ArtifactItem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Tooltip/manual presentation only. No in-world HUD is registered. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,value=Dist.CLIENT)
public final class ArtifactHud {
    private ArtifactHud() {}
    // No in-world HUD: information belongs exclusively to the manual and item tooltips.
    /** A full-screen guide owns the view; inventory/JEI tooltips behind it must not cover its cards. */
    @SubscribeEvent public static void manualTooltip(net.minecraftforge.client.event.RenderTooltipEvent.Pre e){
        if(Minecraft.getInstance().screen instanceof ArtifactManualScreen)e.setCanceled(true);
    }
    @SubscribeEvent public static void tooltip(net.minecraftforge.client.event.RenderTooltipEvent.GatherComponents e){if(e.getItemStack().getItem() instanceof ArtifactItem)e.setMaxWidth(320);}
    @SubscribeEvent public static void levels(net.minecraftforge.event.entity.player.ItemTooltipEvent e){
        if(!(e.getItemStack().getItem() instanceof ArtifactItem item))return;
        var lines=e.getToolTip();Component name=lines.isEmpty()?e.getItemStack().getHoverName():lines.get(0);lines.clear();lines.add(item.kind==io.github.santipdr.specialpickaxes.artifact.ArtifactKind.WORLDBREAKER?name.copy().withStyle(net.minecraft.ChatFormatting.GOLD):name);
        io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.presentation(e.getItemStack(),item.kind,lines,ArtifactTooltipEvents.shiftDown());
    }
}
