package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Presentation content only. No engine budgets, internal counters or client keyboard dependency. */
public final class ArtifactTooltips {
    private ArtifactTooltips(){}
    public static int mode(ItemStack stack){return stack.hasTag()&&stack.getTag().contains("artifactMode")?stack.getTag().getInt("artifactMode"):0;}
    public static void compact(ItemStack stack,ArtifactKind k,List<Component> lines){lines.add(Component.translatable("mining.identity."+k.id+"."+ArtifactInteraction.modeKey(k,mode(stack))).withStyle(ChatFormatting.GRAY));lines.add(Component.translatable("combat.specialpickaxes."+k.id).withStyle(ChatFormatting.DARK_PURPLE));}
    private static void section(List<Component> lines,String heading,Component content){
        if(!lines.isEmpty())lines.add(Component.empty());
        lines.add(Component.translatable("manual4."+heading).withStyle(ChatFormatting.GOLD));lines.add(content.copy().withStyle(ChatFormatting.GRAY));
    }
    public static List<RelicControl.Action> actions(ArtifactKind k,int mode){
        var a=new ArrayList<RelicControl.Action>();boolean region=ArtifactInteraction.regional(k,mode);
        a.add(region?RelicControl.Action.SELECT:RelicControl.Action.ACTIVATE);
        if(region)a.add(RelicControl.Action.CONFIRM);
        if(ArtifactInteraction.modeCount(k)>1)a.add(RelicControl.Action.MODE);
        a.add(RelicControl.Action.CANCEL);
        if(!CompanionActions.handles(k)&&k!=ArtifactKind.AEGIS&&k!=ArtifactKind.INTERREGNUM&&k!=ArtifactKind.MERIDIAN)a.add(RelicControl.Action.PAUSE);
        return a;
    }
    public static void expanded(ItemStack stack,ArtifactKind k,List<Component> lines){
        section(lines,"what",Component.translatable("mining.identity."+k.id+"."+ArtifactInteraction.modeKey(k,mode(stack))));
        section(lines,"how",Component.translatable("mining.how."+k.id+"."+ArtifactInteraction.modeKey(k,mode(stack))));
        if(ArtifactInteraction.modeCount(k)>1)section(lines,"mode",Component.translatable("mode.specialpickaxes."+ArtifactInteraction.modeKey(k,mode(stack))));
        int cost=stack.hasTag()&&stack.getTag().contains("artifactActivationCost")?stack.getTag().getInt("artifactActivationCost"):4;
        int cooldown=stack.hasTag()&&stack.getTag().contains("artifactCooldown")?stack.getTag().getInt("artifactCooldown"):40;
        section(lines,"cost",Component.translatable("manual4.wear",cost,String.format(java.util.Locale.ROOT,"%.1f",cooldown/20D)));
        section(lines,"limits",Component.translatable("mining.limits"));
    }
}
