package io.github.santipdr.specialpickaxes.artifact;

import java.util.Objects;
import java.util.function.Supplier;

/** A deferred equipment action belongs to one exact stack, never an equal replacement. */
public record EquipmentLease<T>(T expected,Supplier<? extends T> current) {
    public EquipmentLease {
        Objects.requireNonNull(expected,"expected equipment");
        Objects.requireNonNull(current,"live equipment lookup");
    }
    public boolean active(){return current.get()==expected;}
}
