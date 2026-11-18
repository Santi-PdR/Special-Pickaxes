package io.github.santipdr.specialpickaxes.client;

import java.util.Optional;

/** Pure layout: keep the HUD on screen and above the hotbar at enlarged GUI scales. */
public final class RelicHudLayout {
    public record Panel(int x,int y,int width,int height,boolean compact){}
    private RelicHudLayout(){}
    public static Optional<Panel> panel(int screenWidth,int screenHeight,boolean survey){
        if(screenWidth<240||screenHeight<140)return Optional.empty();
        int width=Math.min(184,screenWidth/2),height=survey?72:58;
        int x=screenWidth-width-7,y=Math.max(6,Math.min(44,screenHeight-height-48));
        return Optional.of(new Panel(x,y,width,height,width<168));
    }
}
