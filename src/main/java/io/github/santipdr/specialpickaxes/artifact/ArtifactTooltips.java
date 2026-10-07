package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Pure tooltip content: keyboard state and rendering stay on the physical client. */
public final class ArtifactTooltips {
    private ArtifactTooltips(){}
    public static int mode(ItemStack stack){return stack.hasTag()?stack.getTag().getInt("artifactMode"):0;}
    public static void compact(ItemStack stack,ArtifactKind k,List<Component> lines){
        lines.add(Component.translatable("identity.specialpickaxes."+k.id));
        lines.add(Component.translatable("ux.specialpickaxes.mode",Component.translatable("mode.specialpickaxes."+ArtifactInteraction.modeKey(k,mode(stack)))));
        lines.add(Component.translatable("manual.specialpickaxes.shift_hint"));
    }
    public static void expanded(ItemStack stack,ArtifactKind k,List<Component> lines){
        lines.add(Component.translatable("identity.specialpickaxes."+k.id));
        lines.add(Component.translatable("ux.specialpickaxes.mode",Component.translatable("mode.specialpickaxes."+ArtifactInteraction.modeKey(k,mode(stack)))));
        lines.add(Component.translatable("manual.specialpickaxes.heading"));
        lines.add(Component.translatable("tooltip.specialpickaxes."+k.id));
        lines.add(Component.translatable("controls.specialpickaxes."+k.id));
        if(ArtifactInteraction.regional(k,mode(stack)))lines.add(Component.translatable("ux.specialpickaxes.region_controls"));
        lines.add(Component.translatable("manual.specialpickaxes.cancel"));
        int cost=stack.hasTag()&&stack.getTag().contains("artifactActivationCost")?stack.getTag().getInt("artifactActivationCost"):4;
        int cooldown=stack.hasTag()&&stack.getTag().contains("artifactCooldown")?stack.getTag().getInt("artifactCooldown"):40;
        lines.add(Component.translatable("manual.specialpickaxes.cost",cost,cooldown));
        lines.add(Component.translatable("manual.specialpickaxes.enchantments"));
        lines.add(Component.translatable("limits.specialpickaxes."+k.id));
        lines.add(Component.translatable("example.specialpickaxes."+k.id));
        lines.add(Component.translatable(ArtifactInteraction.regional(k,mode(stack))?"manual.specialpickaxes.region_limits":"manual.specialpickaxes.direct_limits"));
        if(stack.hasTag()){
            var t=stack.getTag();
            if(k==ArtifactKind.PALIMPSEST||k==ArtifactKind.CHOIR||k==ArtifactKind.CHRONICLE||k==ArtifactKind.WORLDBREAKER)lines.add(Component.translatable("ux.specialpickaxes.history",t.getInt("artifactMemory"),t.getLong("artifactOldest"),t.getLong("artifactLatest")));
            if(t.contains("artifactSource"))lines.add(Component.translatable("ux.specialpickaxes.volumes",t.getString("artifactSource"),t.getString("artifactTarget")));
            if(t.contains("artifactStatus"))lines.add(Component.translatable("status.specialpickaxes."+t.getString("artifactStatus")));
        }
    }
}
