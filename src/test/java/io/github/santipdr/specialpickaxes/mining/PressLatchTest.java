package io.github.santipdr.specialpickaxes.mining;
import io.github.santipdr.specialpickaxes.artifact.PressLatch;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PressLatchTest {
    @Test void heldAndRepeatedKeyIsOnePress(){var latch=new PressLatch();assertFalse(latch.update(false));assertTrue(latch.update(true));for(int i=0;i<10000;i++)assertFalse(latch.update(true));assertFalse(latch.update(false));assertTrue(latch.update(true));}
    @Test void consumedWhileInMenuDoesNotReactivateOnClosing(){var latch=new PressLatch();latch.update(true);assertFalse(latch.update(true));latch.update(false);assertTrue(latch.update(true));}
}
