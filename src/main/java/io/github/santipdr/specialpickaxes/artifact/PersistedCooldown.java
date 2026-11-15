package io.github.santipdr.specialpickaxes.artifact;

/** A persisted deadline is meaningful only inside the cooldown interval that created it. */
public final class PersistedCooldown {
    private PersistedCooldown(){}

    public static boolean active(long now,long readyAt,int interval){
        if(interval<0)throw new IllegalArgumentException("interval must be non-negative");
        long remaining=readyAt-now;
        // A reset/rollback, malformed NBT or a former dimension clock must not disable a passive forever.
        // The positive difference also rejects subtraction overflow at the long extremes.
        return readyAt>now&&remaining>0&&remaining<=interval;
    }
    /** Remaining ticks are safe to synchronize only after validating the original interval. */
    public static int remaining(long now,long readyAt,int interval){
        return active(now,readyAt,interval)?(int)(readyAt-now):0;
    }
}
