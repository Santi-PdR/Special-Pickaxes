package io.github.santipdr.specialpickaxes.artifact;

import java.util.Objects;

/** Cached wrapped text belongs to a language instance, not only its unchanged locale name. */
public final class ManualLayoutRevision {
    private int width;
    private String content;
    private Object language;
    private boolean initialized;

    /** True means the caller must rebuild its wrapped cards. */
    public boolean refresh(int nextWidth,String nextContent,Object nextLanguage){
        Objects.requireNonNull(nextContent,"content fingerprint");
        Objects.requireNonNull(nextLanguage,"language revision");
        if(initialized&&width==nextWidth&&content.equals(nextContent)&&language==nextLanguage)return false;
        width=nextWidth;content=nextContent;language=nextLanguage;initialized=true;return true;
    }
    public void invalidate(){initialized=false;content=null;language=null;}
}
