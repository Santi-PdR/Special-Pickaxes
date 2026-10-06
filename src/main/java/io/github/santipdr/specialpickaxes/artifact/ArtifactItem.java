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
        super(SpecialPickaxes.TIER,4,-2.6F,new Item.Properties().fireResistant().rarity(Rarity.EPIC));this.kind=kind;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        var tool=player.getItemInHand(hand);
        if(hand!=InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(tool);
        if(player instanceof ServerPlayer p && !ArtifactInteraction.use(p,tool,kind,p.isShiftKeyDown())) return InteractionResultHolder.fail(tool);
        return InteractionResultHolder.sidedSuccess(tool,level.isClientSide);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer()==null?InteractionResult.PASS:use(context.getLevel(),context.getPlayer(),context.getHand()).getResult();
    }
    @Override public float getDestroySpeed(ItemStack stack,BlockState state) { return ArtifactTools.effective(state)?64F:super.getDestroySpeed(stack,state); }
    @Override public boolean isCorrectToolForDrops(BlockState state) { return ArtifactTools.effective(state)&&net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(SpecialPickaxes.TIER,state); }
    @Override public boolean isCorrectToolForDrops(ItemStack stack,BlockState state){return isCorrectToolForDrops(state);}
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
        if(selected && entity instanceof ServerPlayer p && p.tickCount%10==0) {
            if(p.tickCount%100==0)ArtifactState.prune(p,kind);
            // Vanilla inventory synchronization carries the display data; client never decides charge/work.
            var tag=tool.getOrCreateTag();tag.putInt("artifactActivationCost",EnchantmentScaling.activationCost(tool,kind));tag.putInt("artifactCharge",ArtifactState.charge(p,kind));
            tag.putInt("artifactMode",ArtifactState.mode(p,kind));tag.putInt("artifactWork",WorkQueue.remaining(p));
            tag.putInt("artifactMemory",ArtifactState.of(p,kind).getList("memory",net.minecraft.nbt.Tag.TAG_COMPOUND).size());
            ArtifactInteraction.display(p,tool,kind);
        }
    }
    @Override public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || stack.hasTag() && (stack.getTag().getInt("artifactCharge")>=128 || stack.getTag().getInt("artifactWork")>0);
    }
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.specialpickaxes."+kind.id));
        lines.add(Component.translatable("tooltip.specialpickaxes.secondary."+kind.id));
        if(stack.hasTag()&&!ArtifactInteraction.regional(kind)) lines.add(Component.translatable("ux.specialpickaxes.energy",stack.getTag().getInt("artifactCharge")));
        lines.add(Component.translatable("ux.specialpickaxes.mode",Component.translatable("mode.specialpickaxes."+ArtifactInteraction.modeKey(kind,stack.hasTag()?stack.getTag().getInt("artifactMode"):0))));
        lines.add(Component.translatable(ArtifactInteraction.regional(kind)?"ux.specialpickaxes.region_controls":"ux.specialpickaxes.controls"));
        lines.add(Component.translatable("ux.specialpickaxes.cancel_help"));
        int mode=stack.hasTag()?stack.getTag().getInt("artifactMode"):0;
        boolean paid=kind==ArtifactKind.PALIMPSEST||kind==ArtifactKind.KEYSTONE||kind==ArtifactKind.TESSELLATOR||kind==ArtifactKind.WORLDLOOM||kind==ArtifactKind.CHRONICLE&&mode==1||kind==ArtifactKind.WORLDBREAKER&&mode==4;
        String resource=paid?"paid":kind==ArtifactKind.ATLAS||kind==ArtifactKind.CRUCIBLE||kind==ArtifactKind.WORLDBREAKER&&(mode==2||mode==3)?"matter":"mining";
        lines.add(Component.translatable("ux.specialpickaxes.cost."+resource,stack.hasTag()&&stack.getTag().contains("artifactActivationCost")?stack.getTag().getInt("artifactActivationCost"):4));
        if(stack.hasTag()) {
            if(kind==ArtifactKind.PALIMPSEST||kind==ArtifactKind.CHOIR||kind==ArtifactKind.CHRONICLE||kind==ArtifactKind.WORLDBREAKER)lines.add(Component.translatable("ux.specialpickaxes.history",stack.getTag().getInt("artifactMemory"),stack.getTag().getLong("artifactOldest"),stack.getTag().getLong("artifactLatest")));
            lines.add(Component.translatable("status.specialpickaxes."+stack.getTag().getString("artifactStatus")));
            if(kind==ArtifactKind.MERIDIAN && stack.getTag().contains("artifactTarget"))lines.add(Component.translatable("ux.specialpickaxes.link",stack.getTag().getInt("artifactDistance"),stack.getTag().getInt("artifactRotation")));
            if(stack.getTag().contains("artifactSource"))lines.add(Component.translatable("ux.specialpickaxes.volumes",stack.getTag().getString("artifactSource"),stack.getTag().getString("artifactTarget")));
            if(stack.getTag().contains("artifactTransform"))lines.add(Component.translatable("message.specialpickaxes.transform",Component.translatable("mode.specialpickaxes."+stack.getTag().getString("artifactTransform"))));
        }
    }
}
