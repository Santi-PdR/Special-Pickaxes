package io.github.santipdr.specialpickaxes.ability;

import io.github.santipdr.specialpickaxes.PickaxeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public final class AbilityRuntime {
    public static final String DATA = "specialpickaxes";
    private AbilityRuntime() {}
    public static CompoundTag data(ServerPlayer player) {
        CompoundTag parent = player.getPersistentData();
        if (!parent.contains(DATA)) parent.put(DATA, new CompoundTag());
        return parent.getCompound(DATA);
    }
    public static long now(ServerPlayer player) { return player.server.overworld().getGameTime(); }
    public static boolean allowed(ServerPlayer player, String id, BlockPos pos) {
        return player.isAlive() && !player.isSpectator() && player.mayBuild()
            && player.serverLevel().hasChunkAt(pos)
            && player.serverLevel().getWorldBorder().isWithinBounds(pos)
            && player.serverLevel().mayInteract(player, pos)
            && !MinecraftForge.EVENT_BUS.post(new AbilityUseEvent(player, id, pos));
    }
    public static boolean ready(ServerPlayer player, String id) {
        return data(player).getLong(id + "_ready") <= now(player);
    }
    public static void cooldown(ServerPlayer player, ItemStack tool, String id) {
        int ticks = PickaxeConfig.TIMINGS.get(id).cooldown().get();
        data(player).putLong(id + "_ready", now(player) + ticks);
        player.getCooldowns().addCooldown(tool.getItem(), ticks);
    }
    public static boolean activate(ServerPlayer player, ItemStack tool, PickaxeAbility ability) {
        if (!ability.active() || !ready(player, ability.id())
                || !allowed(player, ability.id(), player.blockPosition())) return false;
        var item = tool.getItem();
        int cost = PickaxeConfig.TIMINGS.get(ability.id()).cost().get();
        if (!player.isCreative() && tool.getMaxDamage() - tool.getDamageValue() <= cost) return false;
        if (!ability.activate(player, tool)) return false;
        int ticks = ability.cooldownTicks(player);
        data(player).putLong(ability.id() + "_ready", now(player) + ticks);
        player.getCooldowns().addCooldown(item, ticks);
        tool.hurtAndBreak(cost, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        feedback(player);
        return true;
    }
    public static void feedback(ServerPlayer player) {
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
            SoundSource.PLAYERS, 0.5F, 1.2F);
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1,
            player.getZ(), 12, 0.3, 0.4, 0.3, 0.05);
    }
    public static void message(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("message.specialpickaxes." + key, args), true);
    }
}
