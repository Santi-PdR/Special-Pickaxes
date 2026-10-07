package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Presentation content only. No engine budgets, internal counters or client keyboard dependency. */
public final class ArtifactTooltips {
    private ArtifactTooltips(){}
    public static int mode(ItemStack stack){return stack.hasTag()?stack.getTag().getInt("artifactMode"):0;}
    public static void compact(ItemStack stack,ArtifactKind k,List<Component> lines){lines.add(Component.translatable("identity.specialpickaxes."+k.id).withStyle(ChatFormatting.GRAY));}
    private static void section(List<Component> lines,String heading,Component content){
        if(!lines.isEmpty())lines.add(Component.empty());
        lines.add(Component.translatable("manual4."+heading).withStyle(ChatFormatting.GOLD));lines.add(content.withStyle(ChatFormatting.GRAY));
    }
    public static List<RelicControl.Action> actions(ArtifactKind k,int mode){
        var a=new ArrayList<RelicControl.Action>();boolean region=ArtifactInteraction.regional(k,mode);
        a.add(region?RelicControl.Action.SELECT:RelicControl.Action.ACTIVATE);
        if(region)a.add(RelicControl.Action.CONFIRM);
        if(ArtifactInteraction.modeCount(k)>1)a.add(RelicControl.Action.MODE);
        if(k==ArtifactKind.MERIDIAN||k==ArtifactKind.LODESTAR||k==ArtifactKind.INTERREGNUM||k==ArtifactKind.EVENTIDE||k==ArtifactKind.AEGIS||CompanionActions.handles(k))a.add(RelicControl.Action.SECONDARY);
        a.add(RelicControl.Action.CANCEL);
        if(!CompanionActions.handles(k)&&k!=ArtifactKind.AEGIS&&k!=ArtifactKind.INTERREGNUM&&k!=ArtifactKind.MERIDIAN)a.add(RelicControl.Action.PAUSE);
        return a;
    }
    public static void expanded(ItemStack stack,ArtifactKind k,List<Component> lines){
        section(lines,"what",Component.translatable("identity.specialpickaxes."+k.id));
        section(lines,"how",Component.translatable("play4."+k.id));
        if(ArtifactInteraction.modeCount(k)>1)section(lines,"mode",Component.translatable("mode.specialpickaxes."+ArtifactInteraction.modeKey(k,mode(stack))));
        int cost=stack.hasTag()&&stack.getTag().contains("artifactActivationCost")?stack.getTag().getInt("artifactActivationCost"):4;
        section(lines,"cost",Component.translatable("manual4.wear",cost));
        section(lines,"limits",Component.translatable("warning4."+k.id));
    }
}
