package io.github.santipdr.specialpickaxes.artifact;

/** Pure keyboard focus and horizontal visibility for the manual's copy picker. */
public final class ManualChoiceNavigation {
    private ManualChoiceNavigation(){}
    public static int step(int current,int direction,int count){
        if(count<=0)return -1;
        long position=Math.max(0,Math.min(current,count-1));
        return (int)Math.max(0L,Math.min((long)count-1,position+Integer.signum(direction)));
    }
    public static int reveal(int scroll,int index,int count,int cellWidth,int viewport){
        if(count<=0||index<0||index>=count||cellWidth<=0||viewport<=0)return 0;
        long maximum=Math.max(0L,(long)count*cellWidth-viewport);
        long position=Math.max(0L,Math.min(scroll,maximum));
        long left=(long)index*cellWidth,right=left+cellWidth;
        if(left<position)position=left;
        else if(right>position+viewport)position=right-viewport;
        return (int)Math.min(Integer.MAX_VALUE,Math.max(0L,Math.min(position,maximum)));
    }
}
