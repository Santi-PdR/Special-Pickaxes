package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;

/** Pickup ownership is the saved Owner UUID, not TraceableEntity.getOwner() (the thrower entity). */
public final class DropOwnership {
    private DropOwnership(){}

    public static boolean allows(ServerPlayer player,ItemEntity drop){
        // 1.20.1 exposes a target setter but no pickup-target getter. Use its public save contract,
        // not reflection/private field mappings, and never change or reload the live entity.
        var saved=new CompoundTag();
        drop.addAdditionalSaveData(saved);
        return !saved.hasUUID("Owner")||player.getUUID().equals(saved.getUUID("Owner"));
    }
}
