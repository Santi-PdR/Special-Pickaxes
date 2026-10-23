package io.github.santipdr.specialpickaxes.artifact;

/** Pure scrollbar geometry for the relic manual; safe to exercise in headless GameTests. */
public final class ManualScrollModel {
    private ManualScrollModel(){}

    public static int maxScroll(int contentHeight,int viewportHeight){
        return Math.max(0,contentHeight-Math.max(0,viewportHeight));
    }

    public static int thumbHeight(int trackHeight,int contentHeight){
        if(trackHeight<=0)return 0;
        int proportional=(int)((long)trackHeight*trackHeight/Math.max(trackHeight,contentHeight));
        return Math.min(trackHeight,Math.max(Math.min(10,trackHeight),proportional));
    }

    public static int thumbOffset(int trackHeight,int contentHeight,int scroll){
        int max=maxScroll(contentHeight,trackHeight),travel=trackHeight-thumbHeight(trackHeight,contentHeight);
        if(max<=0||travel<=0)return 0;
        return (int)((long)travel*Math.max(0,Math.min(scroll,max))/max);
    }

    /** Maps the dragged thumb's top edge (in track-local pixels) back into content scroll. */
    public static int scrollFromThumb(int trackHeight,int contentHeight,int thumbTop,int grabOffset){
        int max=maxScroll(contentHeight,trackHeight),travel=trackHeight-thumbHeight(trackHeight,contentHeight);
        if(max<=0||travel<=0)return 0;
        int clampedTop=Math.max(0,Math.min(travel,thumbTop-grabOffset));
        return (int)((long)clampedTop*max/travel);
    }
}
