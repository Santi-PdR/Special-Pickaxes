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
    private static boolean bindingsChecked;
    private static final java.util.Set<String> CHECKED=new java.util.HashSet<>();
    @SubscribeEvent public static void draw(RenderGuiOverlayEvent.Post e){
        if(!Boolean.getBoolean("specialpickaxes.clientSmoke")||e.getOverlay()!=VanillaGuiOverlay.HOTBAR.type())return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||mc.player.tickCount<250)return;
        mc.gui.getChat().clearMessages(false); // Diagnostic capture markers remain in logs, not over the demonstration.
        if(mc.player.getMainHandItem().getItem() instanceof ArtifactItem item){
            if(!bindingsChecked&&!net.minecraft.client.gui.screens.Screen.hasShiftDown()){
                var keys=io.github.santipdr.specialpickaxes.client.RelicKeys.KEYS;
                if(!java.util.Arrays.asList(mc.options.keyMappings).containsAll(java.util.Arrays.asList(keys)))throw new IllegalStateException("Missing registered bindings");
                var bind=keys[0];var old=bind.getKey();
                try{
                    bind.setKey(com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(org.lwjgl.glfw.GLFW.GLFW_KEY_O));
                    var lines=mc.player.getMainHandItem().getTooltipLines(mc.player,net.minecraft.world.item.TooltipFlag.Default.NORMAL);
                    if(!io.github.santipdr.specialpickaxes.artifact.ArtifactInteraction.regional(item.kind,io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.mode(mc.player.getMainHandItem()))&&!lines.get(lines.size()-1).getString().startsWith(bind.getTranslatedKeyMessage().getString()))throw new IllegalStateException("Tooltip ignored rebound key");
                }finally{bind.setKey(old);net.minecraft.client.KeyMapping.resetMapping();}
                bindingsChecked=true;com.mojang.logging.LogUtils.getLogger().info("KEY_BINDINGS_REGISTERED_AND_REBOUND_OK");
            }
            boolean shift=net.minecraft.client.gui.screens.Screen.hasShiftDown();String key=item.kind.id+"-"+io.github.santipdr.specialpickaxes.artifact.ArtifactInteraction.modeKey(item.kind,io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.mode(mc.player.getMainHandItem()))+(shift?"-shift":"-normal");
            if(CHECKED.add(key)){
                var lines=mc.player.getMainHandItem().getTooltipLines(mc.player,net.minecraft.world.item.TooltipFlag.Default.NORMAL);
                if(shift?lines.size()<9:lines.size()!=3)throw new IllegalStateException("Tooltip contract mismatch: "+key+" "+lines.size());
                com.mojang.logging.LogUtils.getLogger().info("TOOLTIP_VALIDATED {} lines={}",key,lines.size());
            }
            e.getGuiGraphics().renderTooltip(mc.font,mc.player.getMainHandItem(),8,18);
        }
    }
}
