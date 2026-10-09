package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
/** Streaming programs share the scheduler; every inspection and mutation consumes one attempt. */
public interface WorkProgram {
    int remaining(); boolean awaiting(); boolean executing(); boolean done();
    void confirm(); boolean loaded(ServerPlayer p); WorkStep next(ServerPlayer p);
    default int attemptsPerTick(ItemStack tool){return EnchantmentScaling.budget(tool);}
    default void reportProgress(ServerPlayer p){}
    default boolean backpressured(ServerPlayer p){return false;}
    default boolean reportPartial(){return true;}
    default void loadMemories(ServerPlayer p){}
}
