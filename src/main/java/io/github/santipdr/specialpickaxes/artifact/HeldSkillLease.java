package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.item.ItemStack;

/** Same-stack Worldbreaker retagging is a source change, not a continuation of old work. */
final class HeldSkillLease {
    private final ItemStack stack;
    private final boolean worldbreaker,hasCopy;
    private final String source;
    private final int mode;
    private HeldSkillLease(ItemStack stack){
        this.stack=stack;worldbreaker=stack.getItem() instanceof ArtifactItem item&&item.kind==ArtifactKind.WORLDBREAKER;
        hasCopy=stack.hasTag()&&stack.getTag().contains("copiedSkill");
        source=hasCopy?stack.getTag().getString("copiedSkill"):"";
        mode=stack.hasTag()?stack.getTag().getInt("copiedMode"):0;
    }
    static HeldSkillLease capture(ItemStack stack){return new HeldSkillLease(stack);}
    boolean valid(){
        if(!worldbreaker)return true;
        return !stack.isEmpty()&&hasCopy==(stack.hasTag()&&stack.getTag().contains("copiedSkill"))
                &&source.equals(stack.hasTag()?stack.getTag().getString("copiedSkill"):"")
                &&mode==(stack.hasTag()?stack.getTag().getInt("copiedMode"):0);
    }
}
