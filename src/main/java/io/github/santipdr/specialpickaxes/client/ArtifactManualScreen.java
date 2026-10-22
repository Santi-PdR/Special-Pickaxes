package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Responsive per-pick guide. Long-form details live here instead of cluttering item tooltips. */
public final class ArtifactManualScreen extends Screen {
    private static final int MAX_WIDTH=820, MAX_HEIGHT=540, PAD=14, LINE_HEIGHT=10;
    private final ArtifactKind kind;
    private final List<ArtifactKind> choices=Arrays.stream(ArtifactKind.playableValues()).toList();
    private int scroll,choiceScroll,contentHeight,layoutWidth=-1;
    private String layoutFingerprint="";
    private List<Card> cards=List.of();
    private record Section(Component heading,List<Component> body,int color) {}
    private record Card(Section section,List<net.minecraft.util.FormattedCharSequence> lines,int height) {}

    public ArtifactManualScreen(ArtifactKind kind){super(Component.translatable("screen.specialpickaxes.manual"));this.kind=kind;}
    @Override public boolean isPauseScreen(){return false;}
    private int panelWidth(){return Math.max(0,Math.min(MAX_WIDTH,width-16));}
    private int panelHeight(){return Math.max(0,Math.min(MAX_HEIGHT,height-16));}
    private int left(){return (width-panelWidth())/2;}
    private int top(){return (height-panelHeight())/2;}
    private boolean compactCopy(){return kind==ArtifactKind.WORLDBREAKER&&panelWidth()<650;}
    private boolean tinyCopy(){return compactCopy()&&panelHeight()<360;}
    private int contentX(){return left()+PAD;}
    private int contentTop(){
        int y=top();
        if(compactCopy())return y+(tinyCopy()?102:164);
        return y+48;
    }
    private int contentBottom(){return top()+panelHeight()-28;}
    private int contentWidth(){
        int w=panelWidth();
        if(kind==ArtifactKind.WORLDBREAKER&&!compactCopy())return Math.max(0,w-2*PAD-232-12);
        return Math.max(0,w-2*PAD-8);
    }

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        renderBackground(g);
        int x=left(),y=top(),w=panelWidth(),h=panelHeight();
        if(w<=0||h<=0){super.render(g,mouseX,mouseY,partial);return;}
        g.fill(x-1,y-1,x+w+1,y+h+1,0xff090e15);
        g.fill(x,y,x+w,y+h,0xf2111925);
        g.fill(x,y,x+w,y+38,0xff223648);
        g.fill(x,y+37,x+w,y+39,0xffd4aa5d);
        g.drawCenteredString(font,title,width/2,y+4,0xffffd477);

        ItemStack icon=new ItemStack(SpecialPickaxes.PICKS.get(kind).get());
        g.renderItem(icon,x+PAD,y+18);
        g.drawString(font,icon.getHoverName(),x+PAD+24,y+22,0xfff1e5c9,false);
        Component headerHint=Component.translatable("screen.specialpickaxes.manual_hint");
        if(w>=600)g.drawString(font,headerHint,x+w-PAD-font.width(headerHint),y+22,0xffc0ccd8,false);

        ItemStack active=displayStack();
        ArtifactKind abilityKind=copiedKind(active);
        int mode=abilityKind==kind?ArtifactTooltips.mode(active):active.hasTag()?active.getTag().getInt("copiedMode"):0;
        String modeKey=ArtifactInteraction.modeKey(abilityKind,mode);
        List<Section> sections=sections(active,abilityKind,mode,modeKey);
        ensureCards(sections,contentWidth(),active,abilityKind,mode,modeKey);

        int contentX=contentX(),contentTop=contentTop(),contentBottom=contentBottom(),contentRight=contentX+contentWidth();
        int maxScroll=Math.max(0,contentHeight-(contentBottom-contentTop));
        scroll=clamp(scroll,maxScroll);
        g.enableScissor(contentX,contentTop,contentRight,contentBottom);
        drawCards(g,contentX,contentTop-scroll,contentWidth());
        g.disableScissor();
        drawScrollBar(g,contentRight+3,contentTop,contentBottom,maxScroll);

        if(kind==ArtifactKind.WORLDBREAKER){
            if(compactCopy())drawCompactChoices(g,active,mouseX,mouseY);
            else drawWideChoices(g,active,mouseX,mouseY);
            String copied=active.hasTag()?active.getTag().getString("copiedSkill"):"";
            net.minecraft.network.chat.MutableComponent selection=copied.isBlank()?Component.translatable("screen.specialpickaxes.copy_native"):Component.translatable("item.specialpickaxes."+copied);
            if(!copied.isBlank()&&ArtifactInteraction.modeCount(abilityKind)>1)selection.append(" · ").append(Component.translatable("mode.specialpickaxes."+modeKey));
            Component state=copied.isBlank()?selection:Component.translatable("screen.specialpickaxes.copy_mode",selection);
            Component selected=Component.translatable("screen.specialpickaxes.selected",state);
            int selectedWidth=Math.max(0,w-2*PAD-font.width(Component.translatable("screen.specialpickaxes.close"))-12);
            g.drawString(font,font.plainSubstrByWidth(selected.getString(),selectedWidth),x+PAD,y+h-20,0xffffd477,false);
        }
        Component close=Component.translatable("screen.specialpickaxes.close");
        g.drawString(font,close,x+w-PAD-font.width(close),y+h-20,0xffaebdca,false);
        super.render(g,mouseX,mouseY,partial);
    }

    private List<Section> sections(ItemStack stack,ArtifactKind abilityKind,int mode,String modeKey){
        var result=new ArrayList<Section>();
        result.add(section("screen.specialpickaxes.main",Component.translatable("mining.identity."+abilityKind.id+"."+modeKey),0xffe4edf4));
        result.add(section("manual4.how",Component.translatable("mining.how."+abilityKind.id+"."+modeKey),0xffc7d4df));
        result.add(section("screen.specialpickaxes.alternate",Component.translatable("alternate.detail.specialpickaxes."+abilityKind.id),0xffffd385));
        result.add(section("screen.specialpickaxes.melee",Component.translatable("melee.specialpickaxes."+kind.id),0xffffc4aa));
        result.add(section("screen.specialpickaxes.passive",Component.translatable("passive.specialpickaxes."+kind.id),0xff9de8c1));
        var curiosInfo=new ArrayList<Component>();curiosInfo.add(Component.translatable("screen.specialpickaxes.curios_passive"));
        curiosInfo.add(Component.translatable("curios.detail.specialpickaxes."+kind.id));
        result.add(new Section(Component.translatable("screen.specialpickaxes.curios"),curiosInfo,0xffb9d4ff));
        if(ArtifactInteraction.modeCount(abilityKind)>1){
            result.add(section("manual4.modes",allModes(abilityKind),0xffd6e0e8));
            result.add(section("manual4.mode",Component.translatable("mode.specialpickaxes."+modeKey),0xffffd079));
        }
        int cost=stack.hasTag()&&stack.getTag().contains("artifactActivationCost")?stack.getTag().getInt("artifactActivationCost"):2;
        int cooldown=stack.hasTag()&&stack.getTag().contains("artifactCooldown")?stack.getTag().getInt("artifactCooldown"):20;
        result.add(section("manual4.cost",Component.translatable("manual4.cost_detail",cost,String.format(Locale.ROOT,"%.1f",cooldown/20D),cost,String.format(Locale.ROOT,"%.1f",Math.max(100,cooldown*5)/20D)),0xffd6e0e8));
        result.add(section("manual4.limits",Component.translatable(abilityKind==ArtifactKind.SEAM_RIPPER?"limits.specialpickaxes.seam_ripper":"mining.limits"),0xffd6e0e8));
        var controls=new ArrayList<Component>();
        for(var action:ArtifactTooltips.actions(stack,kind,mode))controls.add(Component.translatable("key.specialpickaxes."+action.name().toLowerCase(Locale.ROOT)).append(": ").append(RelicKeys.name(action)));
        controls.add(Component.translatable("key.specialpickaxes.manual").append(": I"));
        result.add(new Section(Component.translatable("manual4.controls"),controls,0xffd6e0e8));
        return result;
    }

    private void ensureCards(List<Section> sections,int maxWidth,ItemStack stack,ArtifactKind abilityKind,int mode,String modeKey){
        var tag=stack.getTag();
        String fingerprint=kind+"|"+abilityKind+"|"+mode+"|"+modeKey+"|"
                +(tag==null?"":tag.getInt("artifactActivationCost")+":"+tag.getInt("artifactCooldown")+":"+tag.getString("copiedSkill")+":"+tag.getInt("copiedMode"));
        if(layoutWidth==maxWidth&&fingerprint.equals(layoutFingerprint))return;
        layoutWidth=maxWidth;layoutFingerprint=fingerprint;
        var next=new ArrayList<Card>();int total=0;
        int bodyWidth=Math.max(0,maxWidth-18);
        for(var section:sections){
            var lines=new ArrayList<net.minecraft.util.FormattedCharSequence>();
            for(var body:section.body)lines.addAll(font.split(body,bodyWidth));
            int height=18+Math.max(1,lines.size())*LINE_HEIGHT+7;
            next.add(new Card(section,List.copyOf(lines),height));total+=height+5;
        }
        cards=List.copyOf(next);contentHeight=Math.max(0,total-5);
        scroll=clamp(scroll,Math.max(0,contentHeight-(contentBottom()-contentTop())));
    }

    private void drawCards(GuiGraphics g,int x,int y,int cardWidth){
        int cursor=y;
        for(Card card:cards){
            int cardBottom=cursor+card.height;
            if(cardBottom>contentTop()&&cursor<contentBottom()){
                g.fill(x,cursor,x+cardWidth,cardBottom,0xb91b2733);
                g.fill(x,cursor,x+2,cardBottom,card.section.color);
                g.fill(x+2,cursor,x+cardWidth,cursor+17,0x662a3b4b);
                g.drawString(font,card.section.heading,x+9,cursor+5,card.section.color,false);
                int lineY=cursor+21;
                for(var line:card.lines){g.drawString(font,line,x+9,lineY,card.section.color,false);lineY+=LINE_HEIGHT;}
            }
            cursor=cardBottom+5;
        }
    }

    private void drawScrollBar(GuiGraphics g,int x,int top,int bottom,int maxScroll){
        int height=bottom-top;
        if(height<=0||maxScroll<=0)return;
        g.fill(x,top,x+2,bottom,0xff344555);
        int thumb=Math.max(18,height*height/Math.max(height,contentHeight));
        int travel=height-thumb;
        int thumbY=top+(maxScroll==0?0:travel*scroll/maxScroll);
        g.fill(x-1,thumbY,x+3,thumbY+thumb,0xffd4aa5d);
    }

    private void drawWideChoices(GuiGraphics g,ItemStack active,int mouseX,int mouseY){
        int x=left()+panelWidth()-PAD-220,y=top()+48,bottom=contentBottom();
        g.fill(x-6,y-4,x+220,bottom,0x7f0b1219);
        Component label=Component.translatable("screen.specialpickaxes.copy_help");
        g.drawString(font,label,x,y,0xffc3d1df,false);
        int listY=y+15,listBottom=bottom-4;
        choiceScroll=clamp(choiceScroll,Math.max(0,choices.size()*21-(listBottom-listY)));
        g.enableScissor(x,listY,x+220,listBottom);
        drawChoices(g,x,listY,listBottom,choiceScroll,mouseX,mouseY,active.hasTag()?active.getTag().getString("copiedSkill"):"",1,220);
        g.disableScissor();
    }

    private void drawCompactChoices(GuiGraphics g,ItemStack active,int mouseX,int mouseY){
        int x=left()+PAD,y=top()+43,w=panelWidth()-2*PAD;
        Component label=Component.translatable("screen.specialpickaxes.copy_help");
        g.drawString(font,label,x,y,0xffc3d1df,false);
        int listY=y+13,columns=tinyCopy()?7:3,cellWidth=Math.max(1,w/columns),rowHeight=21;
        int rows=(choices.size()+columns-1)/columns;
        int listBottom=Math.min(contentTop()-4,listY+rows*rowHeight);
        choiceScroll=clamp(choiceScroll,Math.max(0,rows*rowHeight-(listBottom-listY)));
        g.enableScissor(x,listY,x+w,listBottom);
        drawChoices(g,x,listY,listBottom,choiceScroll,mouseX,mouseY,active.hasTag()?active.getTag().getString("copiedSkill"):"",columns,cellWidth,tinyCopy());
        g.disableScissor();
    }

    private Component allModes(ArtifactKind modeKind){
        var text=Component.empty();String[] modes=ArtifactInteraction.modes(modeKind);
        for(int i=0;i<modes.length;i++){if(i>0)text.append(" · ");text.append(Component.translatable("mode.specialpickaxes."+modes[i]));}
        return text;
    }
    private ArtifactKind copiedKind(ItemStack stack){
        if(kind!=ArtifactKind.WORLDBREAKER||!stack.hasTag()||!stack.getTag().contains("copiedSkill"))return kind;
        try{return ArtifactKind.byId(stack.getTag().getString("copiedSkill"));}catch(IllegalArgumentException invalid){return kind;}
    }
    private static Section section(String heading,Component body,int color){return new Section(Component.translatable(heading),List.of(body),color);}
    private ItemStack displayStack(){
        var player=Minecraft.getInstance().player;if(player==null)return ItemStack.EMPTY;
        var hand=player.getMainHandItem();if(hand.getItem() instanceof ArtifactItem item&&item.kind==kind)return hand;
        var equipped=CuriosCompat.find(player);return equipped!=null&&equipped.stack().getItem() instanceof ArtifactItem item&&item.kind==kind?equipped.stack():ItemStack.EMPTY;
    }
    private boolean canChooseCopy(){var player=Minecraft.getInstance().player;return player!=null&&player.getMainHandItem().getItem() instanceof ArtifactItem item&&item.kind==ArtifactKind.WORLDBREAKER;}

    private void drawChoices(GuiGraphics g,int x,int y,int bottom,int offset,int mouseX,int mouseY,String copied,int columns,int cellWidth){
        drawChoices(g,x,y,bottom,offset,mouseX,mouseY,copied,columns,cellWidth,false);
    }
    private void drawChoices(GuiGraphics g,int x,int y,int bottom,int offset,int mouseX,int mouseY,String copied,int columns,int cellWidth,boolean iconsOnly){
        List<Component> tooltip=null;
        for(int i=0;i<choices.size();i++){
            int col=i%columns,row=i/columns,px=x+col*cellWidth,py=y+row*21-offset;
            if(py+19<=y||py>=bottom)continue;
            ArtifactKind source=choices.get(i);
            boolean selected=source.id.equals(copied)||(source==ArtifactKind.WORLDBREAKER&&copied.isBlank());
            boolean hover=mouseX>=px&&mouseX<px+cellWidth&&mouseY>=y&&mouseY<bottom&&mouseY>=py&&mouseY<py+19;
            int inset=columns==1?0:2;
            int boxWidth=Math.max(0,Math.min(cellWidth-3,columns==1?220:cellWidth-4));
            g.fill(px+inset,py,px+inset+boxWidth,py+19,selected?0xff5e4d2d:hover?0xff34485c:0xff1c2935);
            ItemStack stack=new ItemStack(SpecialPickaxes.PICKS.get(source).get());
            g.renderItem(stack,px+inset+(iconsOnly?Math.max(1,(boxWidth-16)/2):1),py+1);
            Component label=source==ArtifactKind.WORLDBREAKER?Component.translatable("screen.specialpickaxes.copy_native"):stack.getHoverName();
            if(!iconsOnly){
                int textWidth=Math.max(0,boxWidth-24);
                String clipped=font.plainSubstrByWidth(label.getString(),textWidth);
                g.drawString(font,clipped,px+inset+22,py+6,selected?0xffffd079:0xffd2dce5,false);
            }
            if(hover)tooltip=choiceTooltip(source,stack,label);
        }
        if(tooltip!=null)g.renderTooltip(font,tooltip,Optional.empty(),mouseX,mouseY);
    }
    private List<Component> choiceTooltip(ArtifactKind source,ItemStack stack,Component label){
        var details=new ArrayList<Component>();details.add(label);
        if(source==ArtifactKind.WORLDBREAKER){details.add(Component.translatable("screen.specialpickaxes.copy_native_help"));return details;}
        String mode=ArtifactInteraction.modeKey(source,0);
        details.add(Component.translatable("screen.specialpickaxes.main").append(": ").append(Component.translatable("mining.identity."+source.id+"."+mode)));
        details.add(Component.translatable("screen.specialpickaxes.alternate").append(": ").append(Component.translatable("alternate.detail.specialpickaxes."+source.id)));
        return details;
    }

    @Override public boolean mouseScrolled(double mouseX,double mouseY,double delta){
        int x=left()+PAD,y=top()+43,w=panelWidth()-2*PAD;
        if(kind==ArtifactKind.WORLDBREAKER){
            if(compactCopy()){
                int listY=y+13,listBottom=contentTop()-4;
                if(mouseX>=x&&mouseX<x+w&&mouseY>=listY&&mouseY<listBottom){
                    int columns=tinyCopy()?7:3,rows=(choices.size()+columns-1)/columns;choiceScroll=clamp(choiceScroll-(int)Math.signum(delta)*36,Math.max(0,rows*21-(listBottom-listY)));return true;
                }
            }else{
                int cx=left()+panelWidth()-PAD-220,cy=top()+63,bottom=contentBottom()-4;
                if(mouseX>=cx&&mouseX<cx+220&&mouseY>=cy&&mouseY<bottom){choiceScroll=clamp(choiceScroll-(int)Math.signum(delta)*42,Math.max(0,choices.size()*21-(bottom-cy)));return true;}
            }
        }
        int cx=contentX(),cy=contentTop(),right=cx+contentWidth();
        if(mouseX>=cx&&mouseX<right+6&&mouseY>=cy&&mouseY<contentBottom()){
            scroll=clamp(scroll-(int)Math.signum(delta)*28,Math.max(0,contentHeight-(contentBottom()-cy)));return true;
        }
        return super.mouseScrolled(mouseX,mouseY,delta);
    }

    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(button==0&&kind==ArtifactKind.WORLDBREAKER&&canChooseCopy()){
            int x,y,columns,cellWidth;
            if(compactCopy()){
                x=left()+PAD;y=top()+56;columns=tinyCopy()?7:3;cellWidth=Math.max(1,(panelWidth()-2*PAD)/columns);
                int bottom=contentTop()-4;
                if(mouseX>=x&&mouseX<x+panelWidth()-2*PAD&&mouseY>=y&&mouseY<bottom){
                    int index=ManualChoiceGrid.indexAt((int)(mouseX-x),(int)(mouseY-y),choiceScroll,columns,cellWidth,21);
                    if(index>=0&&index<choices.size()&&((int)(mouseY-y)%21)<19){RelicKeys.chooseCopy(choices.get(index));return true;}
                }
            }else{
                x=left()+panelWidth()-PAD-220;y=top()+63;columns=1;cellWidth=220;
                int bottom=contentBottom()-4;
                if(mouseX>=x&&mouseX<x+220&&mouseY>=y&&mouseY<bottom){
                    int index=(choiceScroll+(int)(mouseY-y))/21;
                    if(index>=0&&index<choices.size()&&((int)(mouseY-y)%21)<19){RelicKeys.chooseCopy(choices.get(index));return true;}
                }
            }
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }
    @Override public void removed(){
        super.removed();cards=List.of();layoutFingerprint="";layoutWidth=-1;
    }
    private static int clamp(int value,int max){return Math.max(0,Math.min(value,max));}
}
