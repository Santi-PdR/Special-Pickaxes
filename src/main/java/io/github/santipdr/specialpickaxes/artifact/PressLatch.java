package io.github.santipdr.specialpickaxes.artifact;
/** Physical-edge detector: repeats, frame ticks and a held key are not additional presses. */
public final class PressLatch {
    private boolean down;
    public boolean update(boolean pressed){boolean edge=pressed&&!down;down=pressed;return edge;}
}
