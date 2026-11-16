package io.github.santipdr.specialpickaxes.artifact;

/** Fractional wheel/trackpad input for one independently scrollable manual region. */
public final class ManualWheelScroll {
    private double remainder;

    public void reset(){remainder=0D;}

    public int move(int current,int maximum,double delta,int pixelsPerNotch){
        int max=Math.max(0,maximum),position=Math.max(0,Math.min(current,max));
        if(!Double.isFinite(delta)||pixelsPerNotch<=0||max==0){reset();return position;}
        if(delta==0D)return position;
        double movement=-delta*pixelsPerNotch;
        // A reversal should respond immediately, not repay the previous direction's fraction.
        if(remainder*movement<0D)reset();
        double total=Math.max(-Integer.MAX_VALUE,Math.min(Integer.MAX_VALUE,movement+remainder));
        long whole=(long)total;
        long next=(long)position+whole;
        if(next<=0&&total<=0D||next>=max&&total>=0D){reset();return (int)Math.max(0L,Math.min(next,(long)max));}
        remainder=total-whole;
        return (int)next;
    }
}
