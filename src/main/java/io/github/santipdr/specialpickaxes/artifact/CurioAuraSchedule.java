package io.github.santipdr.specialpickaxes.artifact;

/** Pure cadence policy for cosmetic nearby-Curios discovery on the client. */
public final class CurioAuraSchedule {
    public static final int REFRESH_INTERVAL_TICKS=8;
    private CurioAuraSchedule(){}

    public static boolean due(int tick,int lastRefreshTick){
        return lastRefreshTick==Integer.MIN_VALUE||tick<lastRefreshTick
                ||(long)tick-lastRefreshTick>=REFRESH_INTERVAL_TICKS;
    }
}
