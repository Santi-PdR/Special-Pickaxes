package io.github.santipdr.specialpickaxes.artifact;

/** Uniform bounded sampling includes both ends of a long work plan without int overflow. */
public final class PreviewSampling {
    private PreviewSampling(){}
    public static int index(int size,int sample,int samples){
        if(size<=0||samples<=0||samples>size||sample<0||sample>=samples)
            throw new IllegalArgumentException("Invalid preview sample");
        return samples==1?0:(int)((long)sample*(size-1)/(samples-1));
    }
}
