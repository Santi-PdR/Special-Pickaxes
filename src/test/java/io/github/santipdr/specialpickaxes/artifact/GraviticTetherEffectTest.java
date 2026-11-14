package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GraviticTetherEffectTest {
    @Test void ordinaryLevelsRetainTheirAuthoredDrag(){
        assertEquals(.68,GraviticTetherEffect.horizontalDrag(0),1e-9);
        assertEquals(.60,GraviticTetherEffect.horizontalDrag(1),1e-9);
        assertEquals(.52,GraviticTetherEffect.horizontalDrag(2),1e-9);
    }
    @Test void negativeLevelsUseTheWeakestRestraintInsteadOfAcceleration(){
        assertEquals(.68,GraviticTetherEffect.horizontalDrag(-1),1e-9);
        assertEquals(.68,GraviticTetherEffect.horizontalDrag(Integer.MIN_VALUE),1e-9);
    }
    @Test void highLevelsSaturateAtTheExistingFloor(){
        assertEquals(.35,GraviticTetherEffect.horizontalDrag(5),1e-9);
        assertEquals(.35,GraviticTetherEffect.horizontalDrag(Integer.MAX_VALUE),1e-9);
    }
}
