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
    private static final java.util.Set<String> STATUSES=java.util.Set.of("idle","ready","paused","preparing","executing");
    private ArtifactHud() {}
    /** Display-only server NBT. No client countdown or HUD state authorizes an ability. */
    @SubscribeEvent public static void overlay(RenderGuiOverlayEvent.Post event){
        if(event.getOverlay()!=VanillaGuiOverlay.HOTBAR.type())return;
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.screen!=null||mc.options.hideGui||mc.player.isSpectator())return;
        var tool=mc.player.getMainHandItem();
        if(!(tool.getItem() instanceof ArtifactItem item)||!tool.hasTag()||!tool.getTag().contains("hudStatus"))return;
        var tag=tool.getTag();var graphics=event.getGuiGraphics();
        int primary=Math.max(0,Math.min(12000,tag.getInt("hudPrimary")));
        int alternate=Math.max(0,Math.min(48000,tag.getInt("hudAlternate")));
        var survey=io.github.santipdr.specialpickaxes.artifact.SurveyBearing.read(tag,mc.level.dimension().location().toString(),mc.level.getGameTime(),mc.player.position(),mc.player.getUUID());
        var layout=RelicHudLayout.panel(mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),survey.isPresent());
        if(layout.isEmpty())return;
        var panel=layout.get();int width=panel.width(),x=panel.x(),y=panel.y(),height=panel.height();
        int color=0xff000000|item.kind.color;
        graphics.fill(x,y,x+width,y+height,0xc2101720);graphics.fill(x,y,x+2,y+height,color);
        var p=Component.translatable("hud.specialpickaxes.primary",primary==0?Component.translatable("hud.specialpickaxes.ready"):Component.literal(((primary+19)/20)+"s"));
        var a=Component.translatable("hud.specialpickaxes.alternate",alternate==0?Component.translatable("hud.specialpickaxes.ready"):Component.literal(((alternate+19)/20)+"s"));
        graphics.drawString(mc.font,mc.font.plainSubstrByWidth(p.getString(),width-12),x+6,y+5,primary==0?0xffb4f2cd:0xffc3ccd9,false);
        bar(graphics,x+6,y+16,width-12,primary,Math.max(1,tag.getInt("artifactCooldown")),color);
        graphics.drawString(mc.font,mc.font.plainSubstrByWidth(a.getString(),width-12),x+6,y+23,alternate==0?0xffb4f2cd:0xffc3ccd9,false);
        bar(graphics,x+6,y+34,width-12,alternate,Math.max(40,tag.getInt("hudAlternateMax")),color);
        String status=tag.getString("hudStatus");
        if(!STATUSES.contains(status))status="idle";
        Component progress;
        String pauseReason=tag.getString("hudPauseReason");
        if(status.equals("paused")&&(pauseReason.equals("manual")||pauseReason.equals("chunk")||pauseReason.equals("drops")))
            progress=Component.translatable("hud.specialpickaxes.pause."+pauseReason,RelicKeys.name(io.github.santipdr.specialpickaxes.artifact.RelicControl.Action.ACTIVATE));
        else if(!status.equals("idle"))progress=Component.translatable("hud.specialpickaxes.work",Component.translatable("status.specialpickaxes."+status),Math.max(0,tag.getInt("hudCompleted")),Math.max(0,tag.getInt("artifactWork")));
        else if(tag.getInt("hudRecall")>0)progress=Component.translatable("hud.specialpickaxes.recall",Math.min(256,tag.getInt("hudRecall")));
        else if(tag.getBoolean("hudDomain"))progress=Component.translatable("hud.specialpickaxes.domain");
        else progress=Component.translatable("hud.specialpickaxes.idle");
        graphics.drawString(mc.font,mc.font.plainSubstrByWidth(progress.getString(),width-12),x+6,y+44,0xffd5dde9,false);
        if(survey.isPresent()){
            var reading=survey.get();
            var direction=Component.translatable("hud.specialpickaxes.bearing."+reading.direction());
            String vertical=(reading.vertical()>0?"+":"")+reading.vertical();
            var guidance=Component.translatable(panel.compact()?"hud.specialpickaxes.survey_compact":"hud.specialpickaxes.survey",direction,reading.distance(),vertical);
            graphics.drawString(mc.font,mc.font.plainSubstrByWidth(guidance.getString(),width-12),x+6,y+59,0xffffd783,false);
            graphics.fill(x+6,y+56,x+6+(int)((width-12)*reading.remaining()/200D),y+57,0xffc3a65f);
        }
    }
    private static void bar(net.minecraft.client.gui.GuiGraphics graphics,int x,int y,int width,int remaining,int maximum,int color){
        graphics.fill(x,y,x+width,y+3,0xff33404c);
        int filled=remaining<=0?width:(int)Math.round(width*(1-Math.min(1,remaining/(double)maximum)));
        if(filled>0)graphics.fill(x,y,x+filled,y+3,remaining==0?0xff74d3a2:color);
    }
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
