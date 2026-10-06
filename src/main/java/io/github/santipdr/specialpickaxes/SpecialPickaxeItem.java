package io.github.santipdr.specialpickaxes;

import io.github.santipdr.specialpickaxes.ability.AbilityRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public final class SpecialPickaxeItem extends PickaxeItem {
    public final PickaxeDefinition definition;
    public SpecialPickaxeItem(PickaxeDefinition definition) {
        super(definition.tier(), definition.attackDamage(), definition.attackSpeed(), new Item.Properties());
        this.definition = definition;
    }
    public boolean hasAbility(String id) { return definition.abilities().stream().anyMatch(a -> a.id().equals(id)); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || definition.abilities().stream().noneMatch(a -> a.active()))
            return InteractionResultHolder.pass(stack);
        if (player instanceof ServerPlayer server) {
            boolean used = false;
            for (var ability : definition.abilities()) used |= AbilityRuntime.activate(server, stack, ability);
            if (!used) return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
    }
    @Override public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        if (miner instanceof ServerPlayer player && !stack.isEmpty() && !player.isShiftKeyDown()) {
            for (var ability : definition.abilities()) ability.afterMine(player, stack, pos, state);
        }
        return result;
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (selected && entity instanceof ServerPlayer player && player.isAlive() && !player.isSpectator())
            for (var ability : definition.abilities()) ability.tick(player, stack);
    }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.specialpickaxes." + definition.id()));
        lines.add(Component.translatable("tooltip.specialpickaxes.controls"));
    }
}
