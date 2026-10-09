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
    }
    public static List<RelicControl.Action> actions(ArtifactKind k,int mode){
        return actions(ItemStack.EMPTY,k,mode);
    }
    public static List<RelicControl.Action> actions(ItemStack stack,ArtifactKind k,int mode){
        var a=new ArrayList<RelicControl.Action>();
        a.add(RelicControl.Action.ACTIVATE);
        a.add(RelicControl.Action.ALT_SKILL);
        if(ArtifactInteraction.modeCount(k)>1)a.add(RelicControl.Action.MODE);
        return a;
    }
    public static void expanded(ItemStack stack,ArtifactKind k,List<Component> lines){
        String mode=ArtifactInteraction.modeKey(k,mode(stack));
        section(lines,"manual4.what",Component.translatable("mining.identity."+k.id+"."+mode));
        section(lines,"manual4.how",Component.translatable("mining.how."+k.id+"."+mode));
        section(lines,"screen.specialpickaxes.alternate",Component.translatable("alternate.detail.specialpickaxes."+k.id));
        section(lines,"screen.specialpickaxes.melee",Component.translatable("melee.specialpickaxes."+k.id));
        section(lines,"screen.specialpickaxes.passive",Component.translatable("passive.specialpickaxes."+k.id));
        section(lines,"screen.specialpickaxes.curios",Component.translatable("screen.specialpickaxes.curios_passive"));
        if(ArtifactInteraction.modeCount(k)>1){
            var allModes=Component.empty();String[] modes=ArtifactInteraction.modes(k);
            for(int i=0;i<modes.length;i++){if(i>0)allModes.append(" · ");allModes.append(Component.translatable("mode.specialpickaxes."+modes[i]));}
            section(lines,"manual4.modes",allModes);
            section(lines,"manual4.mode",Component.translatable("mode.specialpickaxes."+mode));
        }
        int cost=stack.hasTag()&&stack.getTag().contains("artifactActivationCost")?stack.getTag().getInt("artifactActivationCost"):2;
        int cooldown=stack.hasTag()&&stack.getTag().contains("artifactCooldown")?stack.getTag().getInt("artifactCooldown"):20;
        section(lines,"manual4.cost",Component.translatable("manual4.cost_detail",cost,String.format(Locale.ROOT,"%.1f",cooldown/20D),cost,String.format(Locale.ROOT,"%.1f",Math.max(100,cooldown*5)/20D)));
        section(lines,"manual4.limits",Component.translatable(k==ArtifactKind.SEAM_RIPPER?"limits.specialpickaxes.seam_ripper":"mining.limits"));
        section(lines,"manual4.controls",Component.translatable("tooltip.specialpickaxes.controls"));
    }
    private static void section(List<Component> lines,String heading,Component body){
        lines.add(Component.translatable(heading).withStyle(ChatFormatting.GOLD));
        lines.add(body.copy().withStyle(ChatFormatting.GRAY));
    }
}
