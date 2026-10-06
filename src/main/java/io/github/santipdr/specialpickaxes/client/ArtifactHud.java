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
    @SubscribeEvent public static void levels(net.minecraftforge.event.entity.player.ItemTooltipEvent e){if(e.getItemStack().getItem() instanceof ArtifactItem)e.getToolTip().replaceAll(c->{
        if(c.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t)for(var ench:net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getValues())if(ench.getDescriptionId().equals(t.getKey()))return numericLevels(ench.getFullname(io.github.santipdr.specialpickaxes.artifact.EnchantmentScaling.level(e.getItemStack(),ench)),0);
        return numericLevels(c,0);
    });}
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
        var tag=stack.getTag();int charge=tag==null?0:Math.min(256,Math.max(0,tag.getInt("artifactCharge")));
        int mode=tag==null?1:tag.getInt("artifactMode")+1;int work=tag==null?0:tag.getInt("artifactWork");
        if(io.github.santipdr.specialpickaxes.artifact.ArtifactInteraction.regional(item.kind)){
            int done=tag==null?0:tag.getInt("artifactProgress");charge=done+work==0?0:(int)Math.min(256,256L*done/(done+work));
        }
        int x=mc.getWindow().getGuiScaledWidth()/2-80,y=mc.getWindow().getGuiScaledHeight()-76;
        var gui=event.getGuiGraphics();gui.fill(x,y,x+160,y+4,0x990a1020);
        gui.fill(x,y,x+charge*160/256,y+4,0xff000000|item.kind.color);
        gui.drawString(mc.font,Component.translatable("ux.specialpickaxes.mode",Component.translatable("mode.specialpickaxes."+io.github.santipdr.specialpickaxes.artifact.ArtifactInteraction.modeKey(item.kind,mode-1))),x,y-11,item.kind.color,true);
        if(tag!=null)gui.drawString(mc.font,Component.translatable("status.specialpickaxes."+tag.getString("artifactStatus")),x,y+7,0xc5d6e7,true);
    }
}
