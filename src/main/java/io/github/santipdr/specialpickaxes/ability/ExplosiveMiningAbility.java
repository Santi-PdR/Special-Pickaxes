package io.github.santipdr.specialpickaxes.ability;

import io.github.santipdr.specialpickaxes.PickaxeConfig;
import io.github.santipdr.specialpickaxes.mining.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.Arrays;

public final class ExplosiveMiningAbility implements PickaxeAbility {
    public String id() { return "explosive"; }
    public boolean active() { return true; }
    public boolean activate(ServerPlayer player, ItemStack tool) {
        if (!PickaxeConfig.BLAST_ENABLED.get()) return false;
        HitResult hit = player.pick(Math.min(5, player.getBlockReach()), 0, false);
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) return false;
        BlockPos center = block.getBlockPos();
        float hardness = player.serverLevel().getBlockState(center).getDestroySpeed(player.serverLevel(), center);
        int limit = PickaxeConfig.BLAST_LIMIT.get();
        int radius = PickaxeConfig.BLAST_RADIUS.get();
        int count = MiningSafety.guarded(() -> BoundedTraversal.walk(center, limit, limit * 6 + 1,
            p -> Arrays.stream(Direction.values()).map(p::relative).toList(),
            p -> MiningSafety.breakOne(player, tool, id(), center, p, radius, hardness)).size());
        if (count == 0) return false;
        player.serverLevel().sendParticles(ParticleTypes.EXPLOSION, center.getX() + 0.5, center.getY() + 0.5,
            center.getZ() + 0.5, 1, 0, 0, 0, 0);
        player.serverLevel().playSound(null, center, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.5F, 1.4F);
        return true;
    }
}
