package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/** Bounded native-loot storage with withdrawal-only menu; tagged/capability loot stays native. */
public final class PickaxePouch {
    public static final int MAX_TYPES=64,MAX_PER_TYPE=4096;
    private static final String KEY="materialPouchV1";
    private PickaxePouch(){}
    private static boolean material(ItemStack stack){
        // Tagged/capability-bearing items may carry inventories or arbitrarily large data.
        // Initially accept only ordinary stackable materials, never nested artifacts.
        return !stack.isEmpty()&&stack.isStackable()&&!stack.hasTag()
                &&!(stack.getItem() instanceof ArtifactItem)
                &&!stack.serializeNBT().contains("ForgeCaps");
    }
    /** Internal read-only view. Never exposed to callers; writes must copy before mutation.
     * Avoid cloning all 64 entries for each slot/count in a menu refresh. */
    private static ListTag entries(ItemStack tool){
        return tool.hasTag()?tool.getTag().getList(KEY,Tag.TAG_COMPOUND):new ListTag();
    }
    /** Opaque identity for package-local menu invalidation. Successful writes replace
     * the list (copy-on-write); unrelated tool NBT changes leave this token stable.
     * No caller may mutate or expose it as storage. Legacy/empty pouches return null. */
    static Object contentsToken(ItemStack tool){return tool.hasTag()?tool.getTag().get(KEY):null;}
    private static boolean owner(ItemStack tool){return !tool.isEmpty()&&tool.getItem() instanceof ArtifactItem;}
    /** Returns a separate remainder. Never mutates the supplied loot stack. */
    public static ItemStack insert(ItemStack tool,ItemStack loot){
        if(!owner(tool)||!material(loot))return loot.copy();
        var list=entries(tool);
        if(list.size()>MAX_TYPES)return loot.copy(); // Do not rewrite unknown/corrupt data.
        for(int i=0;i<list.size();i++){
            var entry=list.getCompound(i);var encoded=entry.getCompound("item");
            // Resolve through the same item registry as ItemStack.of, including legacy
            // unqualified IDs. Do not construct unrelated stacks/capabilities per drop.
            var id=net.minecraft.resources.ResourceLocation.tryParse(encoded.getString("id"));
            if(id==null||net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id)!=loot.getItem())continue;
            var prototype=ItemStack.of(encoded);
            if(!ItemStack.isSameItemSameTags(prototype,loot))continue;
            int count=entry.getInt("amount");if(count<1||count>MAX_PER_TYPE)return loot.copy();
            int accepted=Math.min(loot.getCount(),MAX_PER_TYPE-count);
            if(accepted==0)return loot.copy();
            // Rejects (full entry/type table) must not clone all stored NBT.
            // Preserve copy-on-write isolation, but only pay for an accepted insert.
            list=list.copy();list.getCompound(i).putInt("amount",count+accepted);tool.getOrCreateTag().put(KEY,list);
            var rest=loot.copy();rest.shrink(accepted);return rest;
        }
        if(list.size()==MAX_TYPES)return loot.copy();
        int accepted=Math.min(loot.getCount(),MAX_PER_TYPE);
        var prototype=loot.copy();prototype.setCount(1);
        var entry=new CompoundTag();entry.put("item",prototype.save(new CompoundTag()));entry.putInt("amount",accepted);
        list=list.copy();list.add(entry);tool.getOrCreateTag().put(KEY,list);
        var rest=loot.copy();rest.shrink(accepted);return rest;
    }
    public static int types(ItemStack tool){return owner(tool)?Math.min(MAX_TYPES,entries(tool).size()):0;}
    public static ItemStack preview(ItemStack tool,int slot){
        var list=entries(tool);if(!owner(tool)||slot<0||slot>=list.size()||list.size()>MAX_TYPES)return ItemStack.EMPTY;
        var entry=list.getCompound(slot);var stack=ItemStack.of(entry.getCompound("item"));int count=entry.getInt("amount");
        if(!material(stack)||count<1||count>MAX_PER_TYPE)return ItemStack.EMPTY;
        stack.setCount(Math.min(count,stack.getMaxStackSize()));return stack;
    }
    public static int amount(ItemStack tool,int slot){
        if(preview(tool,slot).isEmpty())return 0;
        return entries(tool).getCompound(slot).getInt("amount");
    }
    /** One ordinary stack per request. Caller must put it in inventory or safely retain remainder. */
    public static ItemStack extract(ItemStack tool,int slot,int requested){
        if(!owner(tool)||slot<0||requested<=0)return ItemStack.EMPTY;
        var list=entries(tool);if(list.size()>MAX_TYPES||slot>=list.size())return ItemStack.EMPTY;
        list=list.copy();
        var entry=list.getCompound(slot);var stack=ItemStack.of(entry.getCompound("item"));int count=entry.getInt("amount");
        if(!material(stack)||count<1||count>MAX_PER_TYPE)return ItemStack.EMPTY;
        int taken=Math.min(Math.min(requested,stack.getMaxStackSize()),count);stack.setCount(taken);
        if(taken==count)list.remove(slot);else entry.putInt("amount",count-taken);
        tool.getOrCreateTag().put(KEY,list);return stack;
    }
}
