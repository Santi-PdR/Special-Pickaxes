package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManualLayoutRevisionTest {
    @Test void stableContentDoesNotRewrapEveryFrame(){
        var revision=new ManualLayoutRevision();var language=new Object();
        assertTrue(revision.refresh(600,"pick|mode|bindings",language));
        assertFalse(revision.refresh(600,"pick|mode|bindings",language));
    }
    @Test void resourceReloadInvalidatesEvenAnEqualLocaleName(){
        var revision=new ManualLayoutRevision();var oldLanguage=new String("es_es");var reloadedLanguage=new String("es_es");
        assertEquals(oldLanguage,reloadedLanguage);
        revision.refresh(600,"unchanged",oldLanguage);
        assertTrue(revision.refresh(600,"unchanged",reloadedLanguage));
        assertFalse(revision.refresh(600,"unchanged",reloadedLanguage));
    }
    @Test void resizingAndChangingControlsEachRebuildTheCards(){
        var revision=new ManualLayoutRevision();var language=new Object();
        revision.refresh(600,"R|X",language);
        assertTrue(revision.refresh(320,"R|X",language));
        assertTrue(revision.refresh(320,"O|P",language));
        assertFalse(revision.refresh(320,"O|P",language));
    }
    @Test void reinitializingTheScreenDropsTheOldLayoutWithoutChangingContent(){
        var revision=new ManualLayoutRevision();var language=new Object();
        revision.refresh(600,"same",language);revision.invalidate();
        assertTrue(revision.refresh(600,"same",language));
        assertFalse(revision.refresh(600,"same",language));
        assertThrows(NullPointerException.class,()->revision.refresh(600,null,language));
        assertThrows(NullPointerException.class,()->revision.refresh(600,"same",null));
    }
}
