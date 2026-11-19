package io.github.santipdr.specialpickaxes.artifact;

/** Pure visual interpolation; authoritative expiration never lives on the client. */
public final class StasisLifetime {
    private StasisLifetime(){}
    public static double fraction(int remaining,int total,long elapsed){
        if(total<=0||remaining<=0)return 0;
        long age=Math.max(0,elapsed);
        if(age>=remaining)return 0;
        return Math.min(1D,(remaining-age)/(double)total);
    }
}
