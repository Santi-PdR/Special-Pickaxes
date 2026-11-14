package io.github.santipdr.specialpickaxes.client;

/** Smooth, bounded opacity envelope shared by authored relic particles; independent of the renderer. */
public final class ParticleEnvelope {
    private ParticleEnvelope(){}
    public static float alpha(float age,int lifetime,int fadeIn,int fadeOut){
        if(!Float.isFinite(age)||lifetime<=0||age<=0||age>=lifetime)return 0F;
        float entering=fadeIn<=0?1F:smooth(age/fadeIn);
        float leaving=fadeOut<=0?1F:smooth((lifetime-age)/fadeOut);
        return Math.min(entering,leaving);
    }
    private static float smooth(float value){
        float t=Math.max(0F,Math.min(1F,value));return t*t*(3F-2F*t);
    }
}
