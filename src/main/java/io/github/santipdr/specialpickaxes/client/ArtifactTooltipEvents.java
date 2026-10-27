package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.ArtifactItem;
import io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Shows the full ability guide on Shift without adding a permanent HUD. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,value=Dist.CLIENT)
public final class ArtifactTooltipEvents {
    private ArtifactTooltipEvents(){}
    @SubscribeEvent public static void expandOnShift(ItemTooltipEvent event){
        if(!Screen.hasShiftDown())return;
        var stack=event.getItemStack();
        if(stack.getItem() instanceof ArtifactItem item)
            ArtifactTooltips.expanded(stack,item.kind,event.getToolTip());
    }
}
