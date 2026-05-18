package build.leash.integrations.linear;

/** Lifecycle state of a Linear project. */
public enum LinearProjectState {
    PLANNED("planned"),
    STARTED("started"),
    PAUSED("paused"),
    COMPLETED("completed"),
    CANCELED("canceled"),
    BACKLOG("backlog");

    private final String wire;

    LinearProjectState(String wire) {
        this.wire = wire;
    }

    public String wireValue() {
        return wire;
    }

    public static LinearProjectState fromWire(String wire) {
        if (wire == null) return null;
        for (LinearProjectState s : values()) {
            if (s.wire.equals(wire)) return s;
        }
        return null;
    }
}
