package io.github.santipdr.specialpickaxes.artifact;

/** A strict, shared per-server-tick cap for decorative pickaxe bursts. */
public final class RelicParticleBudget {
    private final int capacity;
    private long tick;
    private boolean initialized;
    private int remaining;

    public RelicParticleBudget(int capacity){
        if(capacity<0)throw new IllegalArgumentException("capacity must be non-negative");
        this.capacity=capacity;
    }

    /** Reserve a whole effect burst only when it fits, so callers never emit partial signatures. */
    public boolean tryReserve(long serverTick,int particles){
        if(particles<0)throw new IllegalArgumentException("particles must be non-negative");
        if(!initialized||tick!=serverTick){initialized=true;tick=serverTick;remaining=capacity;}
        if(particles>remaining)return false;
        remaining-=particles;
        return true;
    }
}
