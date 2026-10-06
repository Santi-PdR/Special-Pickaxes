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
        if(player instanceof ServerPlayer p && !ArtifactActions.use(p,tool,kind,p.isShiftKeyDown())) return InteractionResultHolder.fail(tool);
        return InteractionResultHolder.sidedSuccess(tool,level.isClientSide);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer()==null?InteractionResult.PASS:use(context.getLevel(),context.getPlayer(),context.getHand()).getResult();
    }
    @Override public boolean mineBlock(ItemStack tool,Level level,BlockState state,BlockPos pos,LivingEntity actor) {
        boolean result=super.mineBlock(tool,level,state,pos,actor);
        if(actor instanceof ServerPlayer p && !tool.isEmpty()) MiningObservations.capture(p,tool,kind,pos,state);
        return result;
    }
    @Override public void inventoryTick(ItemStack tool,Level level,Entity entity,int slot,boolean selected) {
        super.inventoryTick(tool,level,entity,slot,selected);
        if(selected && entity instanceof ServerPlayer p && p.tickCount%10==0) {
            // Vanilla inventory synchronization carries the display data; client never decides charge/work.
            var tag=tool.getOrCreateTag();tag.putInt("artifactCharge",ArtifactState.charge(p,kind));
            tag.putInt("artifactMode",ArtifactState.mode(p,kind));tag.putInt("artifactWork",WorkQueue.remaining(p));
            tag.putInt("artifactMemory",ArtifactState.memories(p,kind).size());
            if(WorkQueue.busy(p)) ArtifactFeedback.message(p,"working",WorkQueue.remaining(p));
        }
    }
    @Override public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || stack.hasTag() && (stack.getTag().getInt("artifactCharge")>=128 || stack.getTag().getInt("artifactWork")>0);
    }
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.specialpickaxes."+kind.id));
        lines.add(Component.translatable("tooltip.specialpickaxes.secondary."+kind.id));
        if(stack.hasTag()) lines.add(Component.translatable("tooltip.specialpickaxes.state",stack.getTag().getInt("artifactCharge"),stack.getTag().getInt("artifactMemory"),stack.getTag().getInt("artifactMode")+1));
        lines.add(Component.translatable("tooltip.specialpickaxes.controls"));
    }
}
