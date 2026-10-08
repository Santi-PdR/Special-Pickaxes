package io.github.santipdr.specialpickaxes.artifact;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public final class ArtifactItem extends PickaxeItem {
    public final ArtifactKind kind;
    public ArtifactItem(ArtifactKind kind) {
        super(SpecialPickaxes.TIER,10,-2.6F,new Item.Properties().fireResistant().rarity(kind==ArtifactKind.WORLDBREAKER||kind==ArtifactKind.INTERREGNUM||kind==ArtifactKind.ATLAS||kind==ArtifactKind.CHRONICLE||kind==ArtifactKind.TESSELLATOR?Rarity.EPIC:Rarity.RARE));this.kind=kind;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        var tool=player.getItemInHand(hand);
        if(hand!=InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(tool);
        return InteractionResultHolder.pass(tool);
    }
    @Override public InteractionResult useOn(UseOnContext context) { return InteractionResult.PASS; }
    @Override public float getDestroySpeed(ItemStack stack,BlockState state) { return ArtifactTools.effective(state)?64F:super.getDestroySpeed(stack,state); }
    @Override public boolean isCorrectToolForDrops(BlockState state) { return ArtifactTools.effective(state)&&net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(SpecialPickaxes.TIER,state); }
    @Override public boolean isCorrectToolForDrops(ItemStack stack,BlockState state){return isCorrectToolForDrops(state);}
    @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker) {
        boolean damaged=super.hurtEnemy(stack,target,attacker);
        if(damaged&&attacker instanceof ServerPlayer player) {
            switch(kind) {
                case PALIMPSEST -> { player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION,200,1,false,true,true)); player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION,160,1,false,true,true)); }
                case CHOIR -> { target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,160,2,false,true,true)); target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,100,1,false,true,true)); }
                case EVENTIDE -> target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.LEVITATION,80,1,false,true,true));
                case CRUCIBLE -> target.setSecondsOnFire(8);
                case INTERREGNUM -> { target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,160,4,false,true,true)); target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,100,1,false,true,true)); }
                case WORLDLOOM -> { player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION,240,3,false,true,true)); player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,100,0,false,true,true)); }
                case ICARUS -> { player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING,200,0,false,true,true)); player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.JUMP,160,1,false,true,true)); player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,120,1,false,true,true)); }
                case AXIOM -> { target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING,200,0,false,true,true)); target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,100,1,false,true,true)); }
                case WORLDBREAKER -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,100,1,false,true,true));
                default -> { }
            }
        }
        return damaged;
    }
    @Override public boolean canPerformAction(ItemStack stack,net.minecraftforge.common.ToolAction action) { return ArtifactTools.action(action); }
    @Override public int getEnchantmentLevel(ItemStack stack,net.minecraft.world.item.enchantment.Enchantment e) {
        int raw=EnchantmentScaling.level(stack,e);
        if(e==net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY)return Math.min(46340,raw); // last safe vanilla int square
        if(e==net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE)return EnchantmentScaling.fortune(raw);
        return Math.min(Integer.MAX_VALUE-1,raw); // vanilla Unbreaking adds one before nextInt
    }
    @Override public java.util.Map<net.minecraft.world.item.enchantment.Enchantment,Integer> getAllEnchantments(ItemStack stack){
        var result=new java.util.HashMap<net.minecraft.world.item.enchantment.Enchantment,Integer>();
        for(var e:net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getValues()){int n=getEnchantmentLevel(stack,e);if(n>0)result.put(e,n);}return result;
    }
    @Override public boolean mineBlock(ItemStack tool,Level level,BlockState state,BlockPos pos,LivingEntity actor) {
        boolean result=super.mineBlock(tool,level,state,pos,actor);
        if(actor instanceof ServerPlayer p && !tool.isEmpty()) MiningObservations.capture(p,tool,kind,pos,state);
        return result;
    }
    @Override public void inventoryTick(ItemStack tool,Level level,Entity entity,int slot,boolean selected) {
        super.inventoryTick(tool,level,entity,slot,selected);
        if(selected&&entity instanceof ServerPlayer&&!tool.getOrCreateTag().hasUUID("controlIdentity"))tool.getOrCreateTag().putUUID("controlIdentity",java.util.UUID.randomUUID());
        if(selected && entity instanceof ServerPlayer p && kind==ArtifactKind.LODESTAR)DirectAbilities.recordFootstep(p);
        if(selected && entity instanceof ServerPlayer p && p.tickCount%10==0) {
            if(p.tickCount%100==0)ArtifactState.prune(p,kind);
            // Vanilla inventory synchronization carries the display data; client never authorizes work.
            var tag=tool.getOrCreateTag();tag.putInt("artifactActivationCost",EnchantmentScaling.activationCost(tool,kind));tag.remove("artifactCharge");tag.putInt("artifactCooldown",ArtifactConfig.COOLDOWN.get());
            tag.putInt("artifactMode",ArtifactState.mode(p,kind));tag.putInt("artifactWork",WorkQueue.remaining(p));
            tag.putInt("artifactMemory",ArtifactState.of(p,kind).getList("memory",net.minecraft.nbt.Tag.TAG_COMPOUND).size());
            ArtifactInteraction.display(p,tool,kind);
        }
    }
    @Override public boolean isFoil(ItemStack stack) { return kind==ArtifactKind.WORLDBREAKER||kind==ArtifactKind.MERIDIAN||kind==ArtifactKind.ATLAS; }
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag) {
        ArtifactTooltips.compact(stack,kind,lines);
    }
}
