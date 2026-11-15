package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** A synchronous held utility transaction must not follow a caster moved or disarmed by a claim callback. */
record HeldUtilityContext(ServerPlayer player,ServerLevel level,Vec3 origin,ItemStack tool) {
    static HeldUtilityContext capture(ServerPlayer player,ItemStack tool){
        return new HeldUtilityContext(player,player.serverLevel(),player.position(),tool);
    }
    boolean valid(){
        return player.isAlive()&&!player.isRemoved()&&!player.isSpectator()&&player.mayBuild()
                &&player.serverLevel()==level&&player.position().equals(origin)
                &&player.getMainHandItem()==tool&&!tool.isEmpty();
    }
}
