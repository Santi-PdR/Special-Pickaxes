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
    private int scroll,choiceScroll,contentHeight,layoutWidth=-1,scrollbarGrabOffset;
    private boolean draggingScrollbar;
    private String layoutFingerprint="";
    private List<Card> leftCards=List.of(),rightCards=List.of();
    private record Section(String key,Component heading,List<Component> body,int color) {}
    private record Card(Section section,List<net.minecraft.util.FormattedCharSequence> lines,int height) {}

    public ArtifactManualScreen(ArtifactKind kind){super(Component.translatable("screen.specialpickaxes.manual"));this.kind=kind;this.headerIcon=new ItemStack(SpecialPickaxes.PICKS.get(kind).get());}
    @Override public boolean isPauseScreen(){return false;}
    private boolean selectingCopy(){return kind==ArtifactKind.WORLDBREAKER&&canChooseCopy();}
    private ManualScreenLayout.Layout layout(){return ManualScreenLayout.calculate(width,height,selectingCopy());}

    /** Page-sized keyboard navigation keeps the guide usable without precision mouse scrolling. */
    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){
        var content=layout().content();int maxScroll=Math.max(0,contentHeight-content.height());
        int page=Math.max(24,content.height()-LINE_HEIGHT);
        if(keyCode==org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN){scroll=clamp(scroll+page,maxScroll);return true;}
        if(keyCode==org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP){scroll=clamp(scroll-page,maxScroll);return true;}
        if(keyCode==org.lwjgl.glfw.GLFW.GLFW_KEY_HOME){scroll=0;return true;}
        if(keyCode==org.lwjgl.glfw.GLFW.GLFW_KEY_END){scroll=maxScroll;return true;}
        return super.keyPressed(keyCode,scanCode,modifiers);
    }

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        renderBackground(g);
        var layout=layout();var panel=layout.panel();
        int x=panel.x(),y=panel.y(),w=panel.width(),h=panel.height();
        if(w<=0||h<=0){super.render(g,mouseX,mouseY,partial);return;}
        if(w<180||h<112){g.drawCenteredString(font,Component.translatable("screen.specialpickaxes.resize"),width/2,height/2,0xffd2dce5);super.render(g,mouseX,mouseY,partial);return;}
        ItemStack active=displayStack();
        ArtifactKind abilityKind=copiedKind(active);
        g.fill(x-1,y-1,x+w+1,y+h+1,0xff090e15);
        g.fill(x,y,x+w,y+h,0xf2111925);
        g.fill(x,y,x+w,y+40,0xff1c2b39);
        g.fill(x,y,x+4,y+40,0xffd4aa5d);
        g.fill(x+4,y+39,x+w,y+41,0xffd4aa5d);
        g.drawCenteredString(font,title,width/2,y+4,0xffffd477);

        ItemStack shownIcon=kind==ArtifactKind.WORLDBREAKER&&abilityKind!=kind
                ?new ItemStack(SpecialPickaxes.PICKS.get(abilityKind).get()):headerIcon;
        g.renderItem(shownIcon,x+PAD,y+18);
        int nameX=x+PAD+24;
        Component headerHint=kind==ArtifactKind.WORLDBREAKER&&abilityKind!=kind
                ?Component.translatable("screen.specialpickaxes.copy_mode_header",shownIcon.getHoverName())
                :Component.translatable(selectingCopy()?"screen.specialpickaxes.copy_help":"screen.specialpickaxes.manual_hint");
        int hintX=x+w-PAD-font.width(headerHint);
        int nameMaxWidth=Math.max(0,(w>=600&&font.width(shownIcon.getHoverName())+font.width(headerHint)+PAD*3+24<w
                ?hintX-8:nameX+w-PAD)-nameX);
        String shownName=font.plainSubstrByWidth(shownIcon.getHoverName().getString(),nameMaxWidth);
        g.drawString(font,shownName,nameX,y+22,0xfff1e5c9,false);
        if(w>=600&&font.width(shownIcon.getHoverName())+font.width(headerHint)+PAD*3+24<w)
            g.drawString(font,headerHint,hintX,y+22,0xffc0ccd8,false);

        int mode=abilityKind==kind?ArtifactTooltips.mode(active):active.hasTag()?active.getTag().getInt("copiedMode"):0;
        String modeKey=ArtifactInteraction.modeKey(abilityKind,mode);
        var content=layout.content();int contentX=content.x(),contentTop=content.y(),contentBottom=content.bottom(),contentWidth=content.width();
        boolean twoColumns=layout.twoColumns();
        ensureCards(contentWidth,twoColumns,active,abilityKind,mode,modeKey,contentTop,contentBottom);
        int contentRight=content.right();
        int maxScroll=Math.max(0,contentHeight-content.height());
        scroll=clamp(scroll,maxScroll);
        if(content.width()>0&&content.height()>0){
            g.enableScissor(contentX,contentTop,contentRight,contentBottom);
            if(twoColumns){
                var first=layout.firstColumn();var second=layout.secondColumn();
                drawCards(g,leftCards,first.x(),contentTop-scroll,first.width(),contentTop,contentBottom);
                drawCards(g,rightCards,second.x(),contentTop-scroll,second.width(),contentTop,contentBottom);
            }else drawCards(g,leftCards,contentX,contentTop-scroll,contentWidth,contentTop,contentBottom);
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
        result.add(new Section("screen.specialpickaxes.curios",Component.translatable("screen.specialpickaxes.curios"),curiosInfo,0xffb9d4ff));
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
        result.add(new Section("manual4.controls",Component.translatable("manual4.controls"),controls,0xffd6e0e8));
        return result;
    }

    private void ensureCards(int maxWidth,boolean twoColumns,ItemStack stack,ArtifactKind abilityKind,int mode,String modeKey,int viewportTop,int viewportBottom){
        var tag=stack.getTag();
        String fingerprint=kind+"|"+abilityKind+"|"+mode+"|"+modeKey+"|"
                +(tag==null?"":tag.getInt("artifactActivationCost")+":"+tag.getInt("artifactCooldown")+":"+tag.getString("copiedSkill")+":"+tag.getInt("copiedMode"));
        fingerprint+="|"+twoColumns;
        if(layoutWidth==maxWidth&&fingerprint.equals(layoutFingerprint))return;
        layoutWidth=maxWidth;layoutFingerprint=fingerprint;
        if(maxWidth<32){leftCards=List.of();rightCards=List.of();contentHeight=0;scroll=0;return;}
        List<Section> sections=sections(stack,abilityKind,mode,modeKey);
        var left=new ArrayList<Card>();var right=new ArrayList<Card>();int leftHeight=0,rightHeight=0;
        int cardWidth=twoColumns?Math.max(0,(maxWidth-10)/2):maxWidth;
        int bodyWidth=Math.max(0,cardWidth-18);
        for(var section:sections){
            var lines=new ArrayList<net.minecraft.util.FormattedCharSequence>();
            for(var body:section.body)lines.addAll(font.split(body,bodyWidth));
            int height=18+Math.max(1,lines.size())*LINE_HEIGHT+7;
            Card card=new Card(section,List.copyOf(lines),height);
            boolean onLeft=!twoColumns||switch(section.key){
                case "screen.specialpickaxes.main","manual4.how","screen.specialpickaxes.alternate","manual4.modes","manual4.mode"->true;
                default->false;
            };
            if(onLeft){left.add(card);leftHeight+=height+5;}else{right.add(card);rightHeight+=height+5;}
        }
        leftCards=List.copyOf(left);rightCards=List.copyOf(right);
        contentHeight=Math.max(0,Math.max(leftHeight,rightHeight)-5);
        scroll=clamp(scroll,Math.max(0,contentHeight-Math.max(0,viewportBottom-viewportTop)));
    }

    private void drawCards(GuiGraphics g,List<Card> cards,int x,int y,int cardWidth,int viewportTop,int viewportBottom){
        int cursor=y;
        for(Card card:cards){
            if(cursor>=viewportBottom)break;
            int cardBottom=cursor+card.height;
            if(cardBottom>viewportTop){
                g.fill(x,cursor,x+cardWidth,cardBottom,0xff111a23);
                g.fill(x+1,cursor+1,x+cardWidth-1,cardBottom-1,0xff1b2733);
                g.fill(x,cursor,x+cardWidth,cursor+1,0xff435667);
                g.fill(x,cardBottom-1,x+cardWidth,cardBottom,0xff0b1118);
                g.fill(x,cursor,x+3,cardBottom,card.section.color);
                g.fill(x+3,cursor+1,x+cardWidth-1,cursor+18,0xff243443);
                g.fill(x+3,cursor+17,x+cardWidth-1,cursor+18,0xff314555);
                int headingWidth=Math.max(0,cardWidth-20);
                String heading=font.plainSubstrByWidth(card.section.heading.getString(),headingWidth);
                g.drawString(font,heading,x+10,cursor+5,card.section.color,false);
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
        int thumb=scrollThumbHeight(height);
        int travel=height-thumb;
        int thumbY=top+(maxScroll==0?0:travel*scroll/maxScroll);
        g.fill(x-2,thumbY,x+4,thumbY+thumb,0xffd4aa5d);
        g.fill(x-1,thumbY+1,x+3,thumbY+thumb-1,0xffffdfa0);
    }

    private int scrollThumbHeight(int trackHeight){
        return Math.min(trackHeight,Math.max(10,trackHeight*trackHeight/Math.max(trackHeight,contentHeight)));
    }

    private void setScrollFromThumb(int mouseY,int grabOffset){
        var content=layout().content();int trackHeight=content.height();
        int maxScroll=Math.max(0,contentHeight-trackHeight),thumb=scrollThumbHeight(trackHeight),travel=trackHeight-thumb;
        if(maxScroll<=0||travel<=0)return;
        int thumbY=Math.max(content.y(),Math.min(content.bottom()-thumb,mouseY-grabOffset));
        scroll=clamp((thumbY-content.y())*maxScroll/travel,maxScroll);
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
            ArtifactKind hoveredChoice=null;
            for(int i=0;i<choices.size();i++){
                int px=x+i*CHOICE_CELL-choiceScroll;
                if(px+CHOICE_CELL<=x||px>=right)continue;
                ArtifactKind source=choices.get(i);boolean selected=source.id.equals(copied)||(source==ArtifactKind.WORLDBREAKER&&copied.isBlank());
                boolean hover=mouseX>=px&&mouseX<px+CHOICE_CELL&&mouseY>=y&&mouseY<bottom;
                g.fill(px+1,y+1,px+CHOICE_CELL-2,bottom-1,selected?0xff806b3a:hover?0xff455d70:0xff202e3b);
                ItemStack icon=choiceIcons.computeIfAbsent(source,k->new ItemStack(SpecialPickaxes.PICKS.get(k).get()));
                g.renderItem(icon,px+(CHOICE_CELL-16)/2,y+2);
                if(hover)hoveredChoice=source;
            }
            g.disableScissor();
            if(choiceScroll==0){
                int labelX=x+choices.size()*CHOICE_CELL+8;
                int labelWidth=right-labelX;
                if(labelWidth>=48){
                    ArtifactKind labelKind=hoveredChoice!=null?hoveredChoice:copyChoice(copied);
                    ItemStack labelIcon=choiceIcons.computeIfAbsent(labelKind,k->new ItemStack(SpecialPickaxes.PICKS.get(k).get()));
                    Component label=labelKind==ArtifactKind.WORLDBREAKER
                            ?Component.translatable("screen.specialpickaxes.copy_native"):labelIcon.getHoverName();
                    g.drawString(font,font.plainSubstrByWidth(label.getString(),labelWidth),labelX,y+6,
                            hoveredChoice!=null?0xffd9e5ef:0xffbac7d1,false);
                }
            }
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
    private static ArtifactKind copyChoice(String copied){
        if(copied==null||copied.isBlank())return ArtifactKind.WORLDBREAKER;
        try{return ArtifactKind.byId(copied);}catch(IllegalArgumentException invalid){return ArtifactKind.WORLDBREAKER;}
    }
    private static Section section(String heading,Component body,int color){return new Section(heading,Component.translatable(heading),List.of(body),color);}
    private ItemStack displayStack(){
        var player=Minecraft.getInstance().player;if(player==null)return ItemStack.EMPTY;
        var hand=player.getMainHandItem();if(hand.getItem() instanceof ArtifactItem item&&item.kind==kind)return hand;
        var equipped=CuriosCompat.find(player);return equipped!=null&&equipped.stack().getItem() instanceof ArtifactItem item&&item.kind==kind?equipped.stack():ItemStack.EMPTY;
    }
    private boolean canChooseCopy(){var player=Minecraft.getInstance().player;return player!=null&&player.getMainHandItem().getItem() instanceof ArtifactItem item&&item.kind==ArtifactKind.WORLDBREAKER;}

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
        var content=layout.content();int maxScroll=Math.max(0,contentHeight-content.height());
        int barX=content.right()+2;
        if(button==0&&maxScroll>0&&mouseX>=barX-2&&mouseX<barX+4&&mouseY>=content.y()&&mouseY<content.bottom()){
            int thumb=scrollThumbHeight(content.height());int travel=content.height()-thumb;
            int thumbY=content.y()+(travel==0?0:travel*scroll/maxScroll);
            if(mouseY>=thumbY&&mouseY<thumbY+thumb)scrollbarGrabOffset=(int)mouseY-thumbY;
            else {scrollbarGrabOffset=thumb/2;setScrollFromThumb((int)mouseY,scrollbarGrabOffset);}
            draggingScrollbar=true;return true;
        }
        if(button==0&&kind==ArtifactKind.WORLDBREAKER&&canChooseCopy()&&layout.pickerVisible()
                &&mouseX>=picker.x()&&mouseX<picker.right()&&mouseY>=picker.y()&&mouseY<picker.bottom()){
            int slot=ManualScreenLayout.choiceIndexAt((int)(mouseX-picker.x()),choiceScroll,CHOICE_CELL,choices.size());
            if(slot>=0){RelicKeys.chooseCopy(choices.get(slot));return true;}
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }
    @Override public boolean mouseDragged(double mouseX,double mouseY,int button,double dragX,double dragY){
        if(draggingScrollbar&&button==0){setScrollFromThumb((int)mouseY,scrollbarGrabOffset);return true;}
        return super.mouseDragged(mouseX,mouseY,button,dragX,dragY);
    }
    @Override public boolean mouseReleased(double mouseX,double mouseY,int button){
        if(button==0&&draggingScrollbar){draggingScrollbar=false;return true;}
        return super.mouseReleased(mouseX,mouseY,button);
    }
    @Override public void removed(){
        super.removed();draggingScrollbar=false;leftCards=List.of();rightCards=List.of();choiceIcons.clear();layoutFingerprint="";layoutWidth=-1;
    }
    private static int clamp(int value,int max){return Math.max(0,Math.min(value,max));}
}
