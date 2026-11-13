package io.github.santipdr.specialpickaxes.client;

import net.minecraft.client.gui.screens.Screen;

/** Client-only keyboard state used while building the item tooltip. */
public final class ArtifactTooltipEvents {
    private ArtifactTooltipEvents(){}
    public static boolean shiftDown(){return Screen.hasShiftDown();}
}
