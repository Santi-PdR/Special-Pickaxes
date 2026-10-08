package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.server.level.ServerPlayer;
/** Streaming programs share the scheduler; every inspection and mutation consumes one attempt. */
public interface WorkProgram {
    int remaining(); boolean awaiting(); boolean executing(); boolean done();
    void confirm(); boolean loaded(ServerPlayer p); WorkStep next(ServerPlayer p);
    default boolean backpressured(ServerPlayer p){return false;}
    default boolean reportPartial(){return true;}
    default void loadMemories(ServerPlayer p){}
}
