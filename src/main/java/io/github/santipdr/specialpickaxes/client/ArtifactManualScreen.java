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
    private static final int PAD=14, LINE_HEIGHT=10, CHOICE_CELL=24;
    private final ArtifactKind kind;
    private final ItemStack headerIcon;
    private final List<ArtifactKind> choices=Arrays.stream(ArtifactKind.playableValues()).toList();
    private final Map<ArtifactKind,ItemStack> choiceIcons=new EnumMap<>(ArtifactKind.class);
    private int scroll,choiceScroll,contentHeight,layoutWidth=-1;
    private String layoutFingerprint="";
    private List<Card> cards=List.of();
    private record Section(Component heading,List<Component> body,int color) {}
    private record Card(Section section,List<net.minecraft.util.FormattedCharSequence> lines,int height) {}

    public ArtifactManualScreen(ArtifactKind kind){super(Component.translatable("screen.specialpickaxes.manual"));this.kind=kind;this.headerIcon=new ItemStack(SpecialPickaxes.PICKS.get(kind).get());}
    @Override public boolean isPauseScreen(){return false;}
    private boolean selectingCopy(){return kind==ArtifactKind.WORLDBREAKER&&canChooseCopy();}
    private ManualScreenLayout.Layout layout(){return ManualScreenLayout.calculate(width,height,selectingCopy());}

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        renderBackground(g);
        var layout=layout();var panel=layout.panel();
        int x=panel.x(),y=panel.y(),w=panel.width(),h=panel.height();
        if(w<=0||h<=0){super.render(g,mouseX,mouseY,partial);return;}
        if(w<180||h<112){g.drawCenteredString(font,Component.translatable("screen.specialpickaxes.resize"),width/2,height/2,0xffd2dce5);super.render(g,mouseX,mouseY,partial);return;}
        g.fill(x-1,y-1,x+w+1,y+h+1,0xff090e15);
        g.fill(x,y,x+w,y+h,0xf2111925);
        g.fill(x,y,x+w,y+40,0xff223648);
        g.fill(x,y+39,x+w,y+41,0xffd4aa5d);
        g.drawCenteredString(font,title,width/2,y+4,0xffffd477);

        g.renderItem(headerIcon,x+PAD,y+18);
        g.drawString(font,headerIcon.getHoverName(),x+PAD+24,y+22,0xfff1e5c9,false);
        Component headerHint=Component.translatable(selectingCopy()?"screen.specialpickaxes.copy_help":"screen.specialpickaxes.manual_hint");
        if(w>=600&&font.width(headerIcon.getHoverName())+font.width(headerHint)+PAD*3+24<w)
            g.drawString(font,headerHint,x+w-PAD-font.width(headerHint),y+22,0xffc0ccd8,false);

        ItemStack active=displayStack();
        ArtifactKind abilityKind=copiedKind(active);
        int mode=abilityKind==kind?ArtifactTooltips.mode(active):active.hasTag()?active.getTag().getInt("copiedMode"):0;
        String modeKey=ArtifactInteraction.modeKey(abilityKind,mode);
        var content=layout.content();int contentX=content.x(),contentTop=content.y(),contentBottom=content.bottom(),contentWidth=content.width();
        ensureCards(contentWidth,active,abilityKind,mode,modeKey,contentTop,contentBottom);
        int contentRight=content.right();
        int maxScroll=Math.max(0,contentHeight-content.height());
        scroll=clamp(scroll,maxScroll);
        if(content.width()>0&&content.height()>0){
            g.enableScissor(contentX,contentTop,contentRight,contentBottom);
            drawCards(g,contentX,contentTop-scroll,contentWidth,contentTop,contentBottom);
            g.disableScissor();
            drawScrollBar(g,contentRight+2,contentTop,contentBottom,maxScroll);
        }

        if(kind==ArtifactKind.WORLDBREAKER){
            if(layout.pickerVisible())drawCopyChoices(g,active,layout.picker(),mouseX,mouseY);
            String copied=active.hasTag()?active.getTag().getString("copiedSkill"):"";
            net.minecraft.network.chat.MutableComponent selection=copied.isBlank()?Component.translatable("screen.specialpickaxes.copy_native"):Component.translatable("item.specialpickaxes."+copied);
            if(!copied.isBlank()&&ArtifactInteraction.modeCount(abilityKind)>1)selection.append(" · ").append(Component.translatable("mode.specialpickaxes."+modeKey));
            Component state=copied.isBlank()?selection:Component.translatable("screen.specialpickaxes.copy_mode",selection);
            Component selected=Component.translatable("screen.specialpickaxes.selected",state);
            int selectedWidth=Math.max(0,w-2*PAD-font.width(Component.translatable("screen.specialpickaxes.close"))-12);
            g.drawString(font,font.plainSubstrByWidth(selected.getString(),selectedWidth),x+PAD,layout.footerY(),0xffffd477,false);
        }
        Component close=Component.translatable("screen.specialpickaxes.close");
        g.drawString(font,close,x+w-PAD-font.width(close),layout.footerY(),0xffaebdca,false);
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

    private void ensureCards(int maxWidth,ItemStack stack,ArtifactKind abilityKind,int mode,String modeKey,int viewportTop,int viewportBottom){
        var tag=stack.getTag();
        String fingerprint=kind+"|"+abilityKind+"|"+mode+"|"+modeKey+"|"
                +(tag==null?"":tag.getInt("artifactActivationCost")+":"+tag.getInt("artifactCooldown")+":"+tag.getString("copiedSkill")+":"+tag.getInt("copiedMode"));
        if(layoutWidth==maxWidth&&fingerprint.equals(layoutFingerprint))return;
        layoutWidth=maxWidth;layoutFingerprint=fingerprint;
        if(maxWidth<32){cards=List.of();contentHeight=0;scroll=0;return;}
        List<Section> sections=sections(stack,abilityKind,mode,modeKey);
        var next=new ArrayList<Card>();int total=0;
        int bodyWidth=Math.max(0,maxWidth-18);
        for(var section:sections){
            var lines=new ArrayList<net.minecraft.util.FormattedCharSequence>();
            for(var body:section.body)lines.addAll(font.split(body,bodyWidth));
            int height=18+Math.max(1,lines.size())*LINE_HEIGHT+7;
            next.add(new Card(section,List.copyOf(lines),height));total+=height+5;
        }
        cards=List.copyOf(next);contentHeight=Math.max(0,total-5);
        scroll=clamp(scroll,Math.max(0,contentHeight-Math.max(0,viewportBottom-viewportTop)));
    }

    private void drawCards(GuiGraphics g,int x,int y,int cardWidth,int viewportTop,int viewportBottom){
        int cursor=y;
        for(Card card:cards){
            int cardBottom=cursor+card.height;
            if(cardBottom>viewportTop&&cursor<viewportBottom){
                g.fill(x,cursor,x+cardWidth,cardBottom,0xff111a23);
                g.fill(x+1,cursor+1,x+cardWidth-1,cardBottom-1,0xff1b2733);
                g.fill(x,cursor,x+3,cardBottom,card.section.color);
                g.fill(x+3,cursor+1,x+cardWidth-1,cursor+18,0xff243443);
                g.fill(x+3,cursor+1,x+cardWidth-1,cursor+2,0xff425565);
                g.drawString(font,card.section.heading,x+10,cursor+5,card.section.color,false);
                int lineY=cursor+21;
                for(var line:card.lines){g.drawString(font,line,x+10,lineY,0xffd2dce5,false);lineY+=LINE_HEIGHT;}
            }
            cursor=cardBottom+5;
        }
    }

    private void drawScrollBar(GuiGraphics g,int x,int top,int bottom,int maxScroll){
        int height=bottom-top;
        if(height<=0||maxScroll<=0)return;
        g.fill(x,top,x+2,bottom,0xff344555);
        int thumb=Math.max(1,Math.min(height,height*height/Math.max(height,contentHeight)));
        int travel=height-thumb;
        int thumbY=top+(maxScroll==0?0:travel*scroll/maxScroll);
        g.fill(x-1,thumbY,x+3,thumbY+thumb,0xffd4aa5d);
    }

    private void drawCopyChoices(GuiGraphics g,ItemStack active,ManualScreenLayout.Box picker,int mouseX,int mouseY){
        int x=picker.x(),y=picker.y(),right=picker.right(),bottom=picker.bottom();
        int maxScroll=Math.max(0,choices.size()*CHOICE_CELL-picker.width());
        choiceScroll=clamp(choiceScroll,maxScroll);
        g.fill(x-2,y-1,right+2,bottom+1,0x99303f4d);
        g.fill(x-2,y-1,right+2,y,0xff506476);
        if(picker.width()>0&&picker.height()>0){
            g.enableScissor(x,y,right,bottom);
            String copied=active.hasTag()?active.getTag().getString("copiedSkill"):"";
            List<Component> tooltip=null;
            for(int i=0;i<choices.size();i++){
                int px=x+i*CHOICE_CELL-choiceScroll;
                if(px+CHOICE_CELL<=x||px>=right)continue;
                ArtifactKind source=choices.get(i);boolean selected=source.id.equals(copied)||(source==ArtifactKind.WORLDBREAKER&&copied.isBlank());
                boolean hover=mouseX>=px&&mouseX<px+CHOICE_CELL&&mouseY>=y&&mouseY<bottom;
                g.fill(px+1,y+1,px+CHOICE_CELL-2,bottom-1,selected?0xff806b3a:hover?0xff455d70:0xff202e3b);
                ItemStack icon=choiceIcons.computeIfAbsent(source,k->new ItemStack(SpecialPickaxes.PICKS.get(k).get()));
                g.renderItem(icon,px+(CHOICE_CELL-16)/2,y+2);
                if(hover)tooltip=choiceTooltip(source,icon,source==ArtifactKind.WORLDBREAKER?Component.translatable("screen.specialpickaxes.copy_native"):icon.getHoverName());
            }
            g.disableScissor();
            if(tooltip!=null)g.renderTooltip(font,tooltip,Optional.empty(),mouseX,mouseY);
        }
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

    private List<Component> choiceTooltip(ArtifactKind source,ItemStack stack,Component label){
        var details=new ArrayList<Component>();details.add(label);
        if(source==ArtifactKind.WORLDBREAKER){details.add(Component.translatable("screen.specialpickaxes.copy_native_help"));return details;}
        String mode=ArtifactInteraction.modeKey(source,0);
        details.add(Component.translatable("screen.specialpickaxes.main").append(": ").append(Component.translatable("mining.identity."+source.id+"."+mode)));
        details.add(Component.translatable("screen.specialpickaxes.alternate").append(": ").append(Component.translatable("alternate.detail.specialpickaxes."+source.id)));
        return details;
    }

    @Override public boolean mouseScrolled(double mouseX,double mouseY,double delta){
        var layout=layout();var picker=layout.picker();
        if(layout.pickerVisible()&&mouseX>=picker.x()&&mouseX<picker.right()&&mouseY>=picker.y()&&mouseY<picker.bottom()){
            choiceScroll=clamp(choiceScroll-(int)Math.signum(delta)*CHOICE_CELL*2,Math.max(0,choices.size()*CHOICE_CELL-picker.width()));return true;
        }
        var content=layout.content();
        if(content.width()>0&&content.height()>0&&mouseX>=content.x()&&mouseX<content.right()+6&&mouseY>=content.y()&&mouseY<content.bottom()){
            scroll=clamp(scroll-(int)Math.signum(delta)*28,Math.max(0,contentHeight-content.height()));return true;
        }
        return super.mouseScrolled(mouseX,mouseY,delta);
    }

    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        var layout=layout();var picker=layout.picker();
        if(button==0&&kind==ArtifactKind.WORLDBREAKER&&canChooseCopy()&&layout.pickerVisible()
                &&mouseX>=picker.x()&&mouseX<picker.right()&&mouseY>=picker.y()&&mouseY<picker.bottom()){
            int slot=ManualScreenLayout.choiceIndexAt((int)(mouseX-picker.x()),choiceScroll,CHOICE_CELL,choices.size());
            if(slot>=0){RelicKeys.chooseCopy(choices.get(slot));return true;}
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }
    @Override public void removed(){
        super.removed();cards=List.of();choiceIcons.clear();layoutFingerprint="";layoutWidth=-1;
    }
    private static int clamp(int value,int max){return Math.max(0,Math.min(value,max));}
}
