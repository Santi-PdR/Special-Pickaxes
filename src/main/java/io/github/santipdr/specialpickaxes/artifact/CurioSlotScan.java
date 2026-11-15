package io.github.santipdr.specialpickaxes.artifact;

/** Optional inventory providers must not turn a one-slot bridge into an unbounded tick scan. */
public final class CurioSlotScan {
    public static final int MAX_SLOTS=64;
    private CurioSlotScan(){}
    public static int limit(int reportedSlots){return Math.max(0,Math.min(MAX_SLOTS,reportedSlots));}
}
