package io.github.santipdr.specialpickaxes.artifact;

/** Saturating age for persisted records: future, negative and overflowing dates are invalid, never fresh. */
public final class RecordedAge {
    private RecordedAge(){}
    public static long ticks(long now,long recorded,int unitsPerTick){
        if(unitsPerTick<=0)throw new IllegalArgumentException("units per tick must be positive");
        if(now<0||recorded<0||recorded>now)return Long.MAX_VALUE;
        return (now-recorded)/unitsPerTick;
    }
}
