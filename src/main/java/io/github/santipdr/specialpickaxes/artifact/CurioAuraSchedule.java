package io.github.santipdr.specialpickaxes.artifact;

/** Pure cadence policy for cosmetic nearby-Curios discovery on the client. */
public final class CurioAuraSchedule {
    public static final int REFRESH_INTERVAL_TICKS=8;
    public static final int PARTICLE_INTERVAL_TICKS=5;
    private CurioAuraSchedule(){}

    /** Four aura updates per second keep the signature animated without flooding nearby clients. */
    public static boolean particleDue(int tick){
        return Math.floorMod(tick,PARTICLE_INTERVAL_TICKS)==0;
    }

    public static boolean due(int tick,int lastRefreshTick){
        return lastRefreshTick==Integer.MIN_VALUE||tick<lastRefreshTick
                ||(long)tick-lastRefreshTick>=REFRESH_INTERVAL_TICKS;
    }
}
