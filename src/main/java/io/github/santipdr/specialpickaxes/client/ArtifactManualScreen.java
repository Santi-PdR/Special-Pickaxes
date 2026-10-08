package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Full per-pick guide. Hover tooltips stay short; this manual owns the long-form details. */
public final class ArtifactManualScreen extends Screen {
    private static final int MAX_WIDTH=560,MAX_HEIGHT=360;
    private static final int LINE_HEIGHT=10,SECTION_GAP=7;
    private final ArtifactKind kind;
    private final List<ArtifactKind> choices=Arrays.stream(ArtifactKind.playableValues()).toList();
    private int scroll;
    private int contentHeight;
    private record Section(Component heading,List<Component> body,int color) {}

    public ArtifactManualScreen(ArtifactKind kind){super(Component.translatable("screen.specialpickaxes.manual"));this.kind=kind;}
    @Override public boolean isPauseScreen(){return false;}
    private int panelWidth(){return Math.min(MAX_WIDTH,width-12);}
    private int panelHeight(){return Math.min(MAX_HEIGHT,height-12);}
    private int left(){return (width-panelWidth())/2;}
    private int top(){return (height-panelHeight())/2;}

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        renderBackground(g);int x=left(),y=top(),w=panelWidth(),h=panelHeight();
        g.fill(x,y,x+w,y+h,0xee111925);g.fill(x,y,x+w,y+24,0xff263b4c);
        g.drawCenteredString(font,title,width/2,y+8,0xffe9c96f);
        ItemStack icon=new ItemStack(SpecialPickaxes.PICKS.get(kind).get());
        g.renderItem(icon,x+16,y+32);g.drawString(font,icon.getHoverName(),x+40,y+37,0xffffe3a3,false);

        ItemStack active=displayStack();int mode=ArtifactTooltips.mode(active);String modeKey=ArtifactInteraction.modeKey(kind,mode);
        int contentX=x+16,contentTop=y+62,contentBottom=y+h-31;
        int contentWidth=kind==ArtifactKind.WORLDBREAKER?Math.max(120,Math.min(230,w-270)):w-32;
        List<Section> sections=sections(active,modeKey);
        contentHeight=measure(sections,contentWidth);
        int maxScroll=Math.max(0,contentHeight-(contentBottom-contentTop));scroll=clamp(scroll,maxScroll);
        g.enableScissor(contentX,contentTop,contentX+contentWidth,contentBottom);
        drawSections(g,sections,contentX,contentTop-scroll,contentWidth);
        g.disableScissor();

        if(kind==ArtifactKind.WORLDBREAKER){
            int choicesX=x+w-238,choicesY=y+50,choicesBottom=y+h-25;
            g.drawString(font,Component.translatable("screen.specialpickaxes.copy_help"),choicesX,choicesY-16,0xffdddddd,false);
            g.enableScissor(choicesX,choicesY,choicesX+225,choicesBottom);
            String copied=active.hasTag()?active.getTag().getString("copiedSkill"):"";
            if(canChooseCopy())drawChoices(g,choicesX,choicesY,mouseX,mouseY,copied);
            else g.drawString(font,Component.translatable("screen.specialpickaxes.copy_hand_only"),choicesX,choicesY,0xffb9d4ff,false);
            g.disableScissor();
            var selection=copied.isBlank()?Component.translatable("screen.specialpickaxes.copy_native"):Component.translatable("item.specialpickaxes."+copied);
            g.drawString(font,Component.translatable("screen.specialpickaxes.selected",selection),x+16,y+h-30,0xffe9c96f,false);
        }else if(contentHeight>contentBottom-contentTop){
            g.drawString(font,Component.translatable("screen.specialpickaxes.scroll"),x+w-82,y+h-18,0xffaab7c5,false);
        }
        g.drawString(font,Component.translatable("screen.specialpickaxes.close"),x+16,y+h-18,0xffaab7c5,false);
        super.render(g,mouseX,mouseY,partial);
    }

    private List<Section> sections(ItemStack stack,String modeKey){
        var result=new ArrayList<Section>();
        int mode=ArtifactTooltips.mode(stack);
        result.add(section("screen.specialpickaxes.main",Component.translatable("mining.identity."+kind.id+"."+modeKey),0xffd6e0e8));
        result.add(section("manual4.how",Component.translatable("mining.how."+kind.id+"."+modeKey),0xffd6e0e8));
        result.add(section("screen.specialpickaxes.alternate",Component.translatable("alternate.detail.specialpickaxes."+kind.id),0xffffd079));
        result.add(section("screen.specialpickaxes.melee",Component.translatable("melee.specialpickaxes."+kind.id),0xffffc4aa));
        result.add(section("screen.specialpickaxes.passive",Component.translatable("passive.specialpickaxes."+kind.id),0xff9de8c1));
        var curiosInfo=new ArrayList<Component>();curiosInfo.add(Component.translatable("screen.specialpickaxes.curios_passive"));
        curiosInfo.add(Component.translatable("curios.detail.specialpickaxes."+kind.id));
        result.add(new Section(Component.translatable("screen.specialpickaxes.curios"),curiosInfo,0xffb9d4ff));
        if(ArtifactInteraction.modeCount(kind)>1){
            result.add(section("manual4.modes",allModes(),0xffd6e0e8));
            result.add(section("manual4.mode",Component.translatable("mode.specialpickaxes."+modeKey),0xffffd079));
        }
        int cost=stack.hasTag()&&stack.getTag().contains("artifactActivationCost")?stack.getTag().getInt("artifactActivationCost"):2;
        int cooldown=stack.hasTag()&&stack.getTag().contains("artifactCooldown")?stack.getTag().getInt("artifactCooldown"):20;
        result.add(section("manual4.cost",Component.translatable("manual4.cost_detail",cost,String.format(Locale.ROOT,"%.1f",cooldown/20D),cost,String.format(Locale.ROOT,"%.1f",Math.max(100,cooldown*5)/20D)),0xffd6e0e8));
        result.add(section("manual4.limits",Component.translatable("mining.limits"),0xffd6e0e8));
        var controls=new ArrayList<Component>();
        for(var action:ArtifactTooltips.actions(stack,kind,mode))
            controls.add(Component.translatable("key.specialpickaxes."+action.name().toLowerCase(Locale.ROOT)).append(": ").append(RelicKeys.name(action)));
        controls.add(Component.translatable("key.specialpickaxes.manual").append(": I"));
        result.add(new Section(Component.translatable("manual4.controls"),controls,0xffd6e0e8));
        return result;
    }
    private Component allModes(){
        var text=Component.empty();String[] modes=ArtifactInteraction.modes(kind);
        for(int i=0;i<modes.length;i++){if(i>0)text.append(" · ");text.append(Component.translatable("mode.specialpickaxes."+modes[i]));}
        return text;
    }
    private static Section section(String heading,Component body,int color){return new Section(Component.translatable(heading),List.of(body),color);}
    private int measure(List<Section> sections,int maxWidth){
        int total=0;for(var section:sections){total+=12;for(var body:section.body)total+=Math.max(1,font.split(body,maxWidth).size())*LINE_HEIGHT;total+=SECTION_GAP;}return total;
    }
    private void drawSections(GuiGraphics g,List<Section> sections,int x,int y,int maxWidth){
        for(var section:sections){g.drawString(font,section.heading,x,y,0xffd0ab64,false);y+=12;
            for(var body:section.body){var lines=font.split(body,maxWidth);for(var line:lines){g.drawString(font,line,x,y,section.color,false);y+=LINE_HEIGHT;}}
            y+=SECTION_GAP;
        }
    }
    private ItemStack displayStack(){
        var player=Minecraft.getInstance().player;if(player==null)return ItemStack.EMPTY;
        var hand=player.getMainHandItem();if(hand.getItem() instanceof ArtifactItem item&&item.kind==kind)return hand;
        var equipped=CuriosCompat.find(player);return equipped!=null&&equipped.stack().getItem() instanceof ArtifactItem item&&item.kind==kind?equipped.stack():ItemStack.EMPTY;
    }
    private boolean canChooseCopy(){var player=Minecraft.getInstance().player;return player!=null&&player.getMainHandItem().getItem() instanceof ArtifactItem item&&item.kind==ArtifactKind.WORLDBREAKER;}
    private void drawChoices(GuiGraphics g,int x,int y,int mouseX,int mouseY,String copied){
        for(int i=0;i<choices.size();i++){
            int px=x,py=y+i*19;var source=choices.get(i);
            boolean selected=source.id.equals(copied)||source==ArtifactKind.WORLDBREAKER&&copied.isBlank(),hover=mouseX>=px&&mouseX<px+225&&mouseY>=py&&mouseY<py+18;
            g.fill(px,py,px+225,py+18,selected?0xff5e4d2d:hover?0xff34485c:0xff1c2935);
            var stack=new ItemStack(SpecialPickaxes.PICKS.get(source).get());g.renderItem(stack,px+1,py+1);
            var label=source==ArtifactKind.WORLDBREAKER?Component.translatable("screen.specialpickaxes.copy_native"):stack.getHoverName();
            g.drawString(font,label,px+21,py+5,selected?0xffffd079:0xffd2dce5,false);
            if(hover&&source==ArtifactKind.WORLDBREAKER)g.renderTooltip(font,List.of(label,Component.translatable("screen.specialpickaxes.copy_native_help")),Optional.empty(),mouseX,mouseY);
            else if(hover){String mode=ArtifactInteraction.modeKey(source,0);g.renderTooltip(font,List.of(
                    stack.getHoverName(),
                    Component.translatable("screen.specialpickaxes.main").append(": ").append(Component.translatable("mining.identity."+source.id+"."+mode)),
                    Component.translatable("screen.specialpickaxes.alternate").append(": ").append(Component.translatable("alternate.detail.specialpickaxes."+source.id)),
                    Component.translatable("screen.specialpickaxes.passive").append(": ").append(Component.translatable("passive.specialpickaxes."+source.id))),Optional.empty(),mouseX,mouseY);}
        }
    }
    private static int clamp(int value,int max){return Math.max(0,Math.min(value,max));}
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double delta){
        int x=left(),y=top(),w=panelWidth(),h=panelHeight();int paneWidth=kind==ArtifactKind.WORLDBREAKER?Math.max(120,Math.min(230,w-270)):w-32;
        if(mouseX>=x+16&&mouseX<x+16+paneWidth&&mouseY>=y+62&&mouseY<y+h-31){scroll=clamp(scroll-(int)Math.signum(delta)*24,Math.max(0,contentHeight-(h-93)));return true;}
        return super.mouseScrolled(mouseX,mouseY,delta);
    }
    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(button==0&&kind==ArtifactKind.WORLDBREAKER&&canChooseCopy()){
            int x=left()+panelWidth()-238,y=top()+50;
            for(int i=0;i<choices.size();i++){int py=y+i*19;if(mouseX>=x&&mouseX<x+225&&mouseY>=py&&mouseY<py+18){RelicKeys.chooseCopy(choices.get(i));return true;}}
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }
}
