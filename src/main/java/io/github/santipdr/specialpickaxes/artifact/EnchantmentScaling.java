package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;

/** Raw numeric NBT reader: no byte/short truncation. Operational budgets are separate from item levels. */
public final class EnchantmentScaling {
    private EnchantmentScaling(){}
    public static int level(ItemStack stack,Enchantment enchantment){
        var key=ForgeRegistries.ENCHANTMENTS.getKey(enchantment);if(key==null)return 0;
        int result=0;var list=stack.getEnchantmentTags();
        for(int i=0;i<list.size();i++){var t=list.getCompound(i);if(t.getString("id").equals(key.toString()))result=Math.max(result,(int)Math.max(0,Math.min(Integer.MAX_VALUE,t.getLong("lvl"))));}
        return result;
    }
    public static int throughput(int base,int efficiency,int ceiling){return (int)Math.min(ceiling,base*(1+Math.log1p(Math.max(0,efficiency))/Math.log(2)/4));}
    /** Exact ordinary and 1000+ Fortune; diminishing growth above 4096 avoids unbounded native loot expansion. */
    public static int fortune(int raw){return raw<=4096?Math.max(0,raw):(int)(4096+128*Math.log((double)raw/4096));}
    public static int activationCost(ItemStack stack,ArtifactKind k){int base=ArtifactConfig.COST.get();return k==ArtifactKind.PALIMPSEST||k==ArtifactKind.CHRONICLE||k==ArtifactKind.TESSELLATOR?(int)Math.ceil(base/(1+Math.log1p(level(stack,Enchantments.MENDING)))):base;}
    public static int budget(ItemStack stack){return throughput(ArtifactConfig.PER_PLAYER.get(),level(stack,Enchantments.BLOCK_EFFICIENCY),ArtifactConfig.ENCHANT_BUDGET.get());}
    public static float finiteSpeed(double speed){return (float)Math.max(0,Math.min(1_000_000_000D,Double.isFinite(speed)?speed:1_000_000_000D));}
}
