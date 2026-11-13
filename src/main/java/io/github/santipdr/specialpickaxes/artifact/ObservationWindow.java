package io.github.santipdr.specialpickaxes.artifact;

/** Short-lived break observations use the server clock, not mutable dimension game time. */
final class ObservationWindow {
    private ObservationWindow(){}
    static boolean fresh(int now,int captured){
        // Int subtraction deliberately handles the signed server-tick wrap.
        int elapsed=now-captured;
        return elapsed>=0&&elapsed<=2;
    }
}
