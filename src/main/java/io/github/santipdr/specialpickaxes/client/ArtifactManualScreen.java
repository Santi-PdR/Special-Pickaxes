package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Compact relic guide. Worldbreaker uses its icon grid to choose which alternate skill to echo. */
public final class ArtifactManualScreen extends Screen {
    private static final int WIDTH=500,HEIGHT=278;
    private final ArtifactKind kind;
    private final List<ArtifactKind> choices=Arrays.stream(ArtifactKind.playableValues()).filter(k->k!=ArtifactKind.WORLDBREAKER).toList();
    public ArtifactManualScreen(ArtifactKind kind){super(Component.translatable("screen.specialpickaxes.manual"));this.kind=kind;}
    @Override public boolean isPauseScreen(){return false;}
    private int left(){return (width-WIDTH)/2;}
    private int top(){return (height-HEIGHT)/2;}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        renderBackground(g);int x=left(),y=top();g.fill(x,y,x+WIDTH,y+HEIGHT,0xee111925);g.fill(x,y,x+WIDTH,y+23,0xff263b4c);
        g.drawCenteredString(font,title,width/2,y+7,0xffe9c96f);
        ItemStack icon=new ItemStack(SpecialPickaxes.PICKS.get(kind).get());
        g.renderItem(icon,x+18,y+39);g.renderItemDecorations(font,icon,x+18,y+39);
        g.drawString(font,icon.getHoverName(),x+42,y+43,0xffffe3a3,false);
        int mode=ArtifactTooltips.mode(icon);String key=ArtifactInteraction.modeKey(kind,mode);
        int bodyWidth=kind==ArtifactKind.WORLDBREAKER?220:WIDTH-36;
        drawSection(g,"screen.specialpickaxes.main",Component.translatable("mining.identity."+kind.id+"."+key),x+18,y+75,bodyWidth,0xffd6e0e8);
        drawSection(g,"screen.specialpickaxes.passive",Component.translatable("passive.specialpickaxes."+kind.id),x+18,y+110,bodyWidth,0xff9de8c1);
        drawSection(g,"screen.specialpickaxes.alternate",Component.translatable("alternate.specialpickaxes."+kind.id),x+18,y+145,bodyWidth,0xffffd079);
        drawSection(g,"screen.specialpickaxes.curios",Component.translatable("curio.specialpickaxes."+kind.id),x+18,y+180,bodyWidth,0xffb9d4ff);
        ItemStack held=displayStack();
        if(kind==ArtifactKind.WORLDBREAKER){
            String copied=held.hasTag()?held.getTag().getString("copiedSkill"):"";
            Component chosen=Component.translatable("screen.specialpickaxes.selected",copied.isBlank()?Component.translatable("screen.specialpickaxes.none"):Component.translatable("item.specialpickaxes."+copied));
            g.drawString(font,chosen,x+18,y+218,0xffe9c96f,false);
            g.drawString(font,Component.translatable("screen.specialpickaxes.copy_help"),x+258,y+27,0xffdddddd,false);
            drawChoices(g,x+258,y+44,mouseX,mouseY,copied);
        }else{
            g.drawCenteredString(font,Component.translatable("screen.specialpickaxes.close"),width/2,y+247,0xffaab7c5);
        }
        super.render(g,mouseX,mouseY,partial);
    }
    private ItemStack displayStack(){
        var player=Minecraft.getInstance().player;if(player==null)return ItemStack.EMPTY;
        var hand=player.getMainHandItem();if(hand.getItem() instanceof ArtifactItem item&&item.kind==kind)return hand;
        var equipped=CuriosCompat.find(player);return equipped!=null&&equipped.stack().getItem() instanceof ArtifactItem item&&item.kind==kind?equipped.stack():ItemStack.EMPTY;
    }
    private void drawSection(GuiGraphics g,String heading,Component body,int x,int y,int maxWidth,int color){
        g.drawString(font,Component.translatable(heading),x,y,0xffd0ab64,false);
        var lines=font.split(body,maxWidth);int lineY=y+12;
        for(int i=0;i<Math.min(2,lines.size());i++)g.drawString(font,lines.get(i),x,lineY+i*10,color,false);
    }
    private void drawChoices(GuiGraphics g,int x,int y,int mouseX,int mouseY,String copied){
        for(int i=0;i<choices.size();i++){
            int px=x,py=y+i*19;var source=choices.get(i);
            boolean selected=source.id.equals(copied),hover=mouseX>=px&&mouseX<px+225&&mouseY>=py&&mouseY<py+18;
            g.fill(px,py,px+225,py+18,selected?0xff5e4d2d:hover?0xff34485c:0xff1c2935);
            var stack=new ItemStack(SpecialPickaxes.PICKS.get(source).get());g.renderItem(stack,px+1,py+1);
            g.drawString(font,stack.getHoverName(),px+21,py+5,selected?0xffffd079:0xffd2dce5,false);
            if(hover)g.renderTooltip(font,Component.translatable("alternate.specialpickaxes."+source.id),mouseX,mouseY);
        }
    }
    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(button==0&&kind==ArtifactKind.WORLDBREAKER){
            int x=left()+258,y=top()+44;
            for(int i=0;i<choices.size();i++){
                int px=x,py=y+i*19;
                if(mouseX>=px&&mouseX<px+225&&mouseY>=py&&mouseY<py+18){RelicKeys.chooseCopy(choices.get(i));return true;}
            }
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }
}
