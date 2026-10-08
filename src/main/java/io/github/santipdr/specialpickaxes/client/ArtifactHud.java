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
    @SubscribeEvent public static void tooltip(net.minecraftforge.client.event.RenderTooltipEvent.GatherComponents e){if(e.getItemStack().getItem() instanceof ArtifactItem)e.setMaxWidth(320);}
    @SubscribeEvent public static void levels(net.minecraftforge.event.entity.player.ItemTooltipEvent e){
        if(!(e.getItemStack().getItem() instanceof ArtifactItem item))return;
        var lines=e.getToolTip();Component name=lines.isEmpty()?e.getItemStack().getHoverName():lines.get(0);lines.clear();lines.add(item.kind==io.github.santipdr.specialpickaxes.artifact.ArtifactKind.WORLDBREAKER?name.copy().withStyle(net.minecraft.ChatFormatting.GOLD):name);
        if(net.minecraft.client.gui.screens.Screen.hasShiftDown()){
            io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.expanded(e.getItemStack(),item.kind,lines);
            lines.add(Component.translatable("manual4.controls").withStyle(net.minecraft.ChatFormatting.GOLD));
            for(var action:io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.actions(e.getItemStack(),item.kind,io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.mode(e.getItemStack())))lines.add(Component.translatable("key.specialpickaxes."+action.name().toLowerCase(java.util.Locale.ROOT)).append(": ").append(RelicKeys.name(action)));
            for(var ench:net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getValues()){
                int level=io.github.santipdr.specialpickaxes.artifact.EnchantmentScaling.level(e.getItemStack(),ench);
                if(level>0)lines.add(numericLevels(ench.getFullname(level),0));
            }
        }else {
            io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.compact(e.getItemStack(),item.kind,lines);
            var action=io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.actions(e.getItemStack(),item.kind,io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.mode(e.getItemStack())).get(0);
            lines.add(RelicKeys.name(action).copy().append(" → ").append(Component.translatable("key.specialpickaxes."+action.name().toLowerCase(java.util.Locale.ROOT))).withStyle(net.minecraft.ChatFormatting.AQUA));
        }
    }
    private static Component numericLevels(Component c,int depth){
        if(depth>16)return c;
        if(c.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t && t.getKey().startsWith("enchantment.level.")){
            String n=t.getKey().substring("enchantment.level.".length());try{if(Integer.parseInt(n)>10)return Component.literal(n).setStyle(c.getStyle());}catch(NumberFormatException ignored){}
        }
        var result=c.plainCopy().setStyle(c.getStyle());for(var child:c.getSiblings())result.append(numericLevels(child,depth+1));return result;
    }
}
