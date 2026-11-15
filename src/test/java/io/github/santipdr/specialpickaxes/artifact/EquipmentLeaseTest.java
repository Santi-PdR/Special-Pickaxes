package io.github.santipdr.specialpickaxes.artifact;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class EquipmentLeaseTest {
    @Test void anEqualReplacementDoesNotOwnTheOldLease(){
        String original=new String("pick"),replacement=new String("pick");
        var slot=new AtomicReference<>(original);var lease=new EquipmentLease<>(original,slot::get);
        assertTrue(lease.active());slot.set(replacement);assertFalse(lease.active());
    }
    @Test void unequippingInvalidatesTheLease(){
        var item=new Object();var slot=new AtomicReference<>(item);var lease=new EquipmentLease<>(item,slot::get);
        slot.set(null);assertFalse(lease.active());
    }
    @Test void everyCheckReadsTheCurrentSlotWithoutATickCache(){
        var item=new Object();var slot=new AtomicReference<>(item);var lease=new EquipmentLease<>(item,slot::get);
        assertTrue(lease.active());slot.set(new Object());assertFalse(lease.active());
        slot.set(item);assertTrue(lease.active());
    }
    @Test void missingEquipmentOrLookupCannotCreateALease(){
        assertThrows(NullPointerException.class,()->new EquipmentLease<>(null,()->null));
        assertThrows(NullPointerException.class,()->new EquipmentLease<>(new Object(),null));
    }
}
