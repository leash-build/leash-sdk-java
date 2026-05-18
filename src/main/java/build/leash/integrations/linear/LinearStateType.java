package build.leash.integrations.linear;

/**
 * Linear workflow-state classification on an issue.
 * Wire values match {@code leash-sdk-ts/src/integrations/providers/linear.ts}.
 */
public enum LinearStateType {
    BACKLOG("backlog"),
    UNSTARTED("unstarted"),
    STARTED("started"),
    COMPLETED("completed"),
    CANCELED("canceled"),
    TRIAGE("triage");

    private final String wire;

    LinearStateType(String wire) {
        this.wire = wire;
    }

    public String wireValue() {
        return wire;
    }

    public static LinearStateType fromWire(String wire) {
        if (wire == null) return null;
        for (LinearStateType t : values()) {
            if (t.wire.equals(wire)) return t;
        }
        return null;
    }
}
