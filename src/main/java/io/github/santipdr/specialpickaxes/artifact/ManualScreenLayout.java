package io.github.santipdr.specialpickaxes.artifact;

/** Pure geometry for the relic guide, shared with headless layout tests. */
public final class ManualScreenLayout {
    private static final int MAX_WIDTH=820,MAX_HEIGHT=540,PAD=14;
    private ManualScreenLayout(){}

    public record Box(int x,int y,int width,int height){
        public int right(){return x+width;}
        public int bottom(){return y+height;}
    }
    public record Layout(Box panel,Box content,Box picker,Box firstColumn,Box secondColumn,int footerY,boolean pickerVisible,boolean twoColumns){}

    /** Maps a horizontally scrolled icon strip click to a pick, ignoring each cell's trailing gap. */
    public static int choiceIndexAt(int localX,int pixelScroll,int cellWidth,int choiceCount){
        if(localX<0||pixelScroll<0||cellWidth<=2||choiceCount<=0)return -1;
        int offset=localX+pixelScroll,index=offset/cellWidth;
        return index<choiceCount&&offset%cellWidth<cellWidth-2?index:-1;
    }

    public static Layout calculate(int screenWidth,int screenHeight,boolean showPicker){
        int width=Math.max(0,Math.min(MAX_WIDTH,screenWidth-16));
        int height=Math.max(0,Math.min(MAX_HEIGHT,screenHeight-16));
        int left=(screenWidth-width)/2,top=(screenHeight-height)/2;
        Box panel=new Box(left,top,width,height);
        int footerTop=top+Math.max(0,height-28);
        int contentX=left+Math.min(PAD,width);
        int contentWidth=Math.max(0,width-2*PAD-4);
        int pickerY=top+46;
        boolean pickerVisible=showPicker&&contentWidth>=24&&footerTop-pickerY>=20;
        Box picker=pickerVisible?new Box(contentX,pickerY,contentWidth,20):new Box(contentX,Math.min(pickerY,footerTop),contentWidth,0);
        int desiredContentY=pickerVisible?top+74:top+48;
        int contentY=Math.min(footerTop,desiredContentY);
        Box content=new Box(contentX,contentY,contentWidth,Math.max(0,footerTop-contentY));
        boolean twoColumns=contentWidth>=600;
        int columnWidth=Math.max(0,(contentWidth-10)/2);
        Box firstColumn=twoColumns?new Box(contentX,contentY,columnWidth,content.height()):content;
        Box secondColumn=twoColumns?new Box(contentX+columnWidth+10,contentY,columnWidth,content.height()):new Box(content.right(),contentY,0,content.height());
        return new Layout(panel,content,picker,firstColumn,secondColumn,top+Math.max(0,height-20),pickerVisible,twoColumns);
    }
}
