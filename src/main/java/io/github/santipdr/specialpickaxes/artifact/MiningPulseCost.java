package io.github.santipdr.specialpickaxes.artifact;

/** Match the actual clamped motes emitted by one private mining flash. */
public final class MiningPulseCost {
    private MiningPulseCost(){}
    public static int visible(int requested){return Math.max(0,Math.min(3,requested));}
    public static int perViewer(int primary,int accent){return visible(primary)+visible(accent);}
}
