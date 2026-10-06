package io.github.santipdr.specialpickaxes.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Integration point for claims: cancel to deny an ability/target, including non-block effects. */
@Cancelable
public final class AbilityUseEvent extends Event {
    public final ServerPlayer player;
    public final String ability;
    public final BlockPos target;
    public AbilityUseEvent(ServerPlayer player, String ability, BlockPos target) {
        this.player = player;
        this.ability = ability;
        this.target = target.immutable();
    }
}
