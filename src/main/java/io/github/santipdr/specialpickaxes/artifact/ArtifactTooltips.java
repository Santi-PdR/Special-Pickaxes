package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Presentation content only. No engine budgets, internal counters or client keyboard dependency. */
public final class ArtifactTooltips {
    private ArtifactTooltips(){}
    public static int mode(ItemStack stack){return stack.hasTag()&&stack.getTag().contains("artifactMode")?stack.getTag().getInt("artifactMode"):0;}
    public static void compact(ItemStack stack,ArtifactKind k,List<Component> lines){
        lines.add(Component.translatable("tooltip.specialpickaxes.r",
                Component.translatable("mining.short.specialpickaxes."+k.id)).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.specialpickaxes.x",
                Component.translatable("alternate.specialpickaxes."+k.id)).withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("tooltip.specialpickaxes.passive",
                Component.translatable("passive.specialpickaxes."+k.id)).withStyle(ChatFormatting.GREEN));
    }
    public static List<RelicControl.Action> actions(ArtifactKind k,int mode){
        return actions(ItemStack.EMPTY,k,mode);
    }
    public static List<RelicControl.Action> actions(ItemStack stack,ArtifactKind k,int mode){
        var a=new ArrayList<RelicControl.Action>();boolean region=ArtifactInteraction.regional(k,mode);
        a.add(region?RelicControl.Action.SELECT:RelicControl.Action.ACTIVATE);
        a.add(RelicControl.Action.ALT_SKILL);
        if(region&&stack.hasTag()&&"ready".equals(stack.getTag().getString("artifactStatus")))a.add(RelicControl.Action.CONFIRM);
        if(ArtifactInteraction.modeCount(k)>1)a.add(RelicControl.Action.MODE);
        a.add(RelicControl.Action.CANCEL);
        if(!CompanionActions.handles(k)&&k!=ArtifactKind.AEGIS&&k!=ArtifactKind.INTERREGNUM&&k!=ArtifactKind.MERIDIAN)a.add(RelicControl.Action.PAUSE);
        return a;
    }
    public static void expanded(ItemStack stack,ArtifactKind k,List<Component> lines){
        compact(stack,k,lines);
    }
}
