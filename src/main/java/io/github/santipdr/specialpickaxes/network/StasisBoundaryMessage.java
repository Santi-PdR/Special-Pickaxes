package io.github.santipdr.specialpickaxes.network;

import java.util.UUID;

/** Server-owned visual state for one nearby Stasis domain. */
public record StasisBoundaryMessage(UUID owner,String dimension,long center,int radius,int halfHeight,boolean visible){}
