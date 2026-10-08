package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.ArtifactItem;
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
    @SubscribeEvent public static void tooltip(net.minecraftforge.client.event.RenderTooltipEvent.GatherComponents e){if(e.getItemStack().getItem() instanceof ArtifactItem)e.setMaxWidth(320);}
    @SubscribeEvent public static void levels(net.minecraftforge.event.entity.player.ItemTooltipEvent e){
        if(!(e.getItemStack().getItem() instanceof ArtifactItem item))return;
        var lines=e.getToolTip();Component name=lines.isEmpty()?e.getItemStack().getHoverName():lines.get(0);lines.clear();lines.add(item.kind==io.github.santipdr.specialpickaxes.artifact.ArtifactKind.WORLDBREAKER?name.copy().withStyle(net.minecraft.ChatFormatting.GOLD):name);
        io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.compact(e.getItemStack(),item.kind,lines);
    }
}
