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
    @SubscribeEvent public static void tooltip(net.minecraftforge.client.event.RenderTooltipEvent.GatherComponents e){if(e.getItemStack().getItem() instanceof ArtifactItem)e.setMaxWidth(280);}
    @SubscribeEvent public static void levels(net.minecraftforge.event.entity.player.ItemTooltipEvent e){
        if(!(e.getItemStack().getItem() instanceof ArtifactItem item))return;
        var lines=e.getToolTip();Component name=lines.isEmpty()?e.getItemStack().getHoverName():lines.get(0);lines.clear();lines.add(name);
        if(net.minecraft.client.gui.screens.Screen.hasShiftDown()){
            io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.expanded(e.getItemStack(),item.kind,lines);
            for(var ench:net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getValues()){
                int level=io.github.santipdr.specialpickaxes.artifact.EnchantmentScaling.level(e.getItemStack(),ench);
                if(level>0)lines.add(numericLevels(ench.getFullname(level),0));
            }
        }else io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.compact(e.getItemStack(),item.kind,lines);
    }
    private static Component numericLevels(Component c,int depth){
        if(depth>16)return c;
        if(c.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t && t.getKey().startsWith("enchantment.level.")){
            String n=t.getKey().substring("enchantment.level.".length());try{if(Integer.parseInt(n)>10)return Component.literal(n).setStyle(c.getStyle());}catch(NumberFormatException ignored){}
        }
        var result=c.plainCopy().setStyle(c.getStyle());for(var child:c.getSiblings())result.append(numericLevels(child,depth+1));return result;
    }
    @SubscribeEvent public static void render(RenderGuiOverlayEvent.Post event) {
        if(event.getOverlay()!=VanillaGuiOverlay.HOTBAR.type()) return;
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.options.hideGui) return;
        var stack=mc.player.getMainHandItem();
        if(!(stack.getItem() instanceof ArtifactItem item)) return;
        var tag=stack.getTag();int mode=tag==null?0:tag.getInt("artifactMode");
        int x=mc.getWindow().getGuiScaledWidth()/2-80,y=mc.getWindow().getGuiScaledHeight()-62;
        var gui=event.getGuiGraphics();
        gui.drawString(mc.font,Component.translatable("ux.specialpickaxes.mode",Component.translatable("mode.specialpickaxes."+io.github.santipdr.specialpickaxes.artifact.ArtifactInteraction.modeKey(item.kind,mode))),x,y-11,item.kind.color,true);
        if(tag!=null)gui.drawString(mc.font,Component.translatable("status.specialpickaxes."+tag.getString("artifactStatus")),x,y+2,0xc5d6e7,true);
    }
}
