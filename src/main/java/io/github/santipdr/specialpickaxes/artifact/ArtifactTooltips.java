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
        var ability=abilityKind(stack,k);
        lines.add(Component.translatable("tooltip.specialpickaxes.r", Component.keybind("key.specialpickaxes.activate"),
                Component.translatable("mining.short.specialpickaxes."+ability.id)).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.specialpickaxes.x", Component.keybind("key.specialpickaxes.alt_skill"),
                Component.translatable("alternate.specialpickaxes."+ability.id)).withStyle(ChatFormatting.AQUA));
        if(k==ArtifactKind.WORLDBREAKER&&stack.hasTag()&&stack.getTag().contains("copiedSkill")){
            String source=stack.getTag().getString("copiedSkill");
            try{
                var copied=ArtifactKind.byId(source);var pickName=Component.translatable("item.specialpickaxes."+source);
                String mode=copyModeKey(stack,copied);
                lines.add((mode.isEmpty()?Component.translatable("tooltip.specialpickaxes.copying",pickName)
                        :Component.translatable("tooltip.specialpickaxes.copying_mode",pickName,Component.translatable("mode.specialpickaxes."+mode)))
                        .withStyle(ChatFormatting.GOLD));
            }
            catch(IllegalArgumentException ignored){}
        }
    }
    /** The compact tooltip and manual read the exact copied mode saved on this tool. */
    public static String copyModeKey(ItemStack stack){
        if(!stack.hasTag()||!stack.getTag().contains("copiedSkill"))return "";
        try{return copyModeKey(stack,ArtifactKind.byId(stack.getTag().getString("copiedSkill")));}
        catch(IllegalArgumentException invalid){return "";}
    }
    private static String copyModeKey(ItemStack stack,ArtifactKind copied){
        return copied==ArtifactKind.WORLDBREAKER||ArtifactInteraction.modeCount(copied)<2?""
                :ArtifactInteraction.modeKey(copied,stack.getTag().getInt("copiedMode"));
    }
    public static List<RelicControl.Action> actions(ArtifactKind k,int mode){
        return actions(ItemStack.EMPTY,k,mode);
    }
    public static List<RelicControl.Action> actions(ItemStack stack,ArtifactKind k,int mode){
        var a=new ArrayList<RelicControl.Action>();
        a.add(RelicControl.Action.ACTIVATE);
        a.add(RelicControl.Action.ALT_SKILL);
        if(ArtifactInteraction.modeCount(k)>1&&abilityKind(stack,k)==k)a.add(RelicControl.Action.MODE);
        return a;
    }
    /** Source identity is shared by the expanded tooltip and manual; invalid NBT stays native. */
    public static ArtifactKind abilityKind(ItemStack stack,ArtifactKind nativeKind){
        return ArtifactSkills.feedbackKind(stack,nativeKind);
    }
    public static int abilityMode(ItemStack stack,ArtifactKind nativeKind){
        return abilityKind(stack,nativeKind)==nativeKind?mode(stack):stack.getTag().getInt("copiedMode");
    }
    public static void expanded(ItemStack stack,ArtifactKind k,List<Component> lines){
        var ability=abilityKind(stack,k);
        String mode=ArtifactInteraction.modeKey(ability,abilityMode(stack,k));
        section(lines,"manual4.what",Component.translatable("mining.identity."+ability.id+"."+mode));
        section(lines,"manual4.how",Component.translatable("mining.how."+ability.id+"."+mode));
        section(lines,"screen.specialpickaxes.alternate",Component.translatable("alternate.detail.specialpickaxes."+ability.id));
        section(lines,"manual4.tool_profile",Component.translatable("manual4.tool_profile_detail"));
        section(lines,"screen.specialpickaxes.melee",Component.translatable("melee.specialpickaxes."+k.id));
        section(lines,"screen.specialpickaxes.passive",Component.translatable("passive.specialpickaxes."+k.id));
        section(lines,"screen.specialpickaxes.curios",Component.translatable("screen.specialpickaxes.curios_passive"));
        if(ArtifactInteraction.modeCount(ability)>1){
            var allModes=Component.empty();String[] modes=ArtifactInteraction.modes(ability);
            for(int i=0;i<modes.length;i++){if(i>0)allModes.append(" · ");allModes.append(Component.translatable("mode.specialpickaxes."+modes[i]));}
            section(lines,"manual4.modes",allModes);
            section(lines,"manual4.mode",Component.translatable("mode.specialpickaxes."+mode));
        }
        section(lines,"manual4.cost",costDetail(stack));
        section(lines,"manual4.limits",Component.translatable(ability==ArtifactKind.SEAM_RIPPER?"limits.specialpickaxes.seam_ripper":"mining.limits"));
        boolean canCycle=actions(stack,k,mode(stack)).contains(RelicControl.Action.MODE);
        section(lines,"manual4.controls",canCycle
                ?Component.translatable("tooltip.specialpickaxes.controls",Component.keybind("key.specialpickaxes.activate"),Component.keybind("key.specialpickaxes.alt_skill"),Component.keybind("key.specialpickaxes.mode"),Component.keybind("key.specialpickaxes.manual"))
                :Component.translatable("tooltip.specialpickaxes.controls_single",Component.keybind("key.specialpickaxes.activate"),Component.keybind("key.specialpickaxes.alt_skill"),Component.keybind("key.specialpickaxes.manual")));
    }
    public static Component costDetail(ItemStack stack){
        int cost=stack.hasTag()&&stack.getTag().contains("artifactActivationCost")?Math.max(0,stack.getTag().getInt("artifactActivationCost")):2;
        int cooldown=stack.hasTag()&&stack.getTag().contains("artifactCooldown")?Math.max(0,stack.getTag().getInt("artifactCooldown")):20;
        return Component.translatable("manual4.cost_detail",Component.keybind("key.specialpickaxes.activate"),cost,
                String.format(Locale.ROOT,"%.1f",cooldown/20D),Component.keybind("key.specialpickaxes.alt_skill"),cost,
                String.format(Locale.ROOT,"%.1f",Math.max(100L,(long)cooldown*5)/20D));
    }
    private static void section(List<Component> lines,String heading,Component body){
        lines.add(Component.translatable(heading).withStyle(ChatFormatting.GOLD));
        lines.add(body.copy().withStyle(ChatFormatting.GRAY));
    }
}
