package io.github.santipdr.specialpickaxes.artifact;

/** Persisted input times must not lock controls after clock rollback or malformed NBT. */
final class InputThrottle {
    private InputThrottle(){}
    static boolean recent(long now,long previous,int interval){
        if(interval<0)throw new IllegalArgumentException("interval must be non-negative");
        long elapsed=now-previous;
        return previous<=now&&elapsed>=0&&elapsed<interval;
    }
}
