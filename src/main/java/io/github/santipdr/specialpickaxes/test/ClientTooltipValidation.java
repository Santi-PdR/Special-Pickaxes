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
                var registered=java.util.Arrays.asList(mc.options.keyMappings);
                for(var action:io.github.santipdr.specialpickaxes.artifact.RelicControl.Action.values())
                    if(action!=io.github.santipdr.specialpickaxes.artifact.RelicControl.Action.CONFIRM&&action!=io.github.santipdr.specialpickaxes.artifact.RelicControl.Action.PAUSE&&action!=io.github.santipdr.specialpickaxes.artifact.RelicControl.Action.SELECT&&action!=io.github.santipdr.specialpickaxes.artifact.RelicControl.Action.CANCEL&&!registered.contains(keys[action.ordinal()]))throw new IllegalStateException("Missing registered binding: "+action);
                if(!registered.contains(io.github.santipdr.specialpickaxes.client.RelicKeys.MANUAL))throw new IllegalStateException("Missing manual binding");
                var bind=keys[0];var old=bind.getKey();
                var alternateBind=keys[io.github.santipdr.specialpickaxes.artifact.RelicControl.Action.ALT_SKILL.ordinal()];var oldAlternate=alternateBind.getKey();
                try{
                    bind.setKey(com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(org.lwjgl.glfw.GLFW.GLFW_KEY_O));
                    var lines=mc.player.getMainHandItem().getTooltipLines(mc.player,net.minecraft.world.item.TooltipFlag.Default.NORMAL);
                    if(!io.github.santipdr.specialpickaxes.artifact.ArtifactInteraction.regional(item.kind,io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.mode(mc.player.getMainHandItem()))
                            &&lines.stream().noneMatch(line->line.getString().startsWith(bind.getTranslatedKeyMessage().getString())))
                        throw new IllegalStateException("Tooltip ignored rebound key: "+bind.getTranslatedKeyMessage().getString()+" in "+lines);
                    String how=io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.how(io.github.santipdr.specialpickaxes.artifact.ArtifactKind.SEAM_RIPPER,"connected").getString();
                    if(!how.contains(" "+bind.getTranslatedKeyMessage().getString()+".")||how.contains("%1$s"))
                        throw new IllegalStateException("Instruction prose ignored rebound key: "+how);
                    alternateBind.setKey(com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(org.lwjgl.glfw.GLFW.GLFW_KEY_P));
                    String alternate=io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.alternate(io.github.santipdr.specialpickaxes.artifact.ArtifactKind.EVENTIDE).getString();
                    if(!alternate.contains(" "+alternateBind.getTranslatedKeyMessage().getString()+" ")
                            ||!alternate.contains(" "+bind.getTranslatedKeyMessage().getString()+" ")||alternate.contains("%3$s"))
                        throw new IllegalStateException("Alternate prose ignored rebound controls: "+alternate);
                    com.mojang.logging.LogUtils.getLogger().info("ALTERNATE_BINDINGS_REBOUND_OK");
                    com.mojang.logging.LogUtils.getLogger().info("INSTRUCTION_BINDINGS_REBOUND_OK");
                }finally{bind.setKey(old);alternateBind.setKey(oldAlternate);net.minecraft.client.KeyMapping.resetMapping();}
                bindingsChecked=true;com.mojang.logging.LogUtils.getLogger().info("KEY_BINDINGS_REGISTERED_AND_REBOUND_OK");
            }
            boolean shift=net.minecraft.client.gui.screens.Screen.hasShiftDown();String key=item.kind.id+"-"+io.github.santipdr.specialpickaxes.artifact.ArtifactInteraction.modeKey(item.kind,io.github.santipdr.specialpickaxes.artifact.ArtifactTooltips.mode(mc.player.getMainHandItem()))+(shift?"-shift":"-normal");
            if(CHECKED.add(key)){
                // Include Forge ItemTooltipEvent: checking only appendHoverText hid an event that erased SHIFT.
                var lines=mc.player.getMainHandItem().getTooltipLines(mc.player,net.minecraft.world.item.TooltipFlag.Default.NORMAL);
                if(shift?lines.size()<10:lines.size()!=3)throw new IllegalStateException("Final stack tooltip contract mismatch: "+key+" "+lines.size());
                boolean details=lines.stream().anyMatch(line->line.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text&&text.getKey().equals("manual4.what"));
                if(details!=shift)throw new IllegalStateException("Forge tooltip event lost or leaked expanded details: "+key);
                int wrappedLines=lines.stream().mapToInt(line->mc.font.split(line,320).size()).sum();
                if(wrappedLines*mc.font.lineHeight+16>mc.getWindow().getGuiScaledHeight())
                    throw new IllegalStateException("Tooltip overflows the captured viewport: "+key+" wrappedLines="+wrappedLines);
                com.mojang.logging.LogUtils.getLogger().info("TOOLTIP_VIEWPORT_FITS {} wrappedLines={}",key,wrappedLines);
                com.mojang.logging.LogUtils.getLogger().info("TOOLTIP_STACK_EVENT_VALIDATED {} lines={}",key,lines.size());
                com.mojang.logging.LogUtils.getLogger().info("TOOLTIP_VALIDATED {} lines={}",key,lines.size());
            }
            e.getGuiGraphics().renderTooltip(mc.font,mc.player.getMainHandItem(),8,18);
        }
    }
}
