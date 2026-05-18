package build.leash.integrations;

import build.leash.integrations.calendar.CalendarIntegration;
import build.leash.integrations.drive.DriveIntegration;
import build.leash.integrations.gmail.GmailIntegration;
import build.leash.integrations.linear.LinearIntegration;
import build.leash.internal.Transport;

import java.util.Objects;

/**
 * {@code leash.integrations()} — typed provider surface + generic
 * {@link #provider(String)} escape hatch.
 *
 * <p>Mirrors the TS {@code leash.integrations} namespace and Python
 * {@code IntegrationsNamespace}. Method chaining stays Java-idiomatic:
 *
 * <pre>{@code
 * leash.integrations().gmail().listMessages(...)
 * leash.integrations().linear().listIssues(...)
 * leash.integrations().provider("slack").call("post_message", ...)
 * }</pre>
 */
public final class IntegrationsNamespace {

    private final Transport transport;
    private final GmailIntegration gmail;
    private final CalendarIntegration calendar;
    private final DriveIntegration drive;
    private final LinearIntegration linear;

    public IntegrationsNamespace(Transport transport) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.gmail = new GmailIntegration(transport);
        this.calendar = new CalendarIntegration(transport);
        this.drive = new DriveIntegration(transport);
        this.linear = new LinearIntegration(transport);
    }

    public GmailIntegration gmail() {
        return gmail;
    }

    public CalendarIntegration calendar() {
        return calendar;
    }

    /** Alias of {@link #calendar()} — matches the long-form provider id the TS SDK exposes. */
    public CalendarIntegration googleCalendar() {
        return calendar;
    }

    public DriveIntegration drive() {
        return drive;
    }

    /** Alias of {@link #drive()} — matches the long-form provider id the TS SDK exposes. */
    public DriveIntegration googleDrive() {
        return drive;
    }

    public LinearIntegration linear() {
        return linear;
    }

    /**
     * Generic escape hatch — call any provider action the SDK doesn't model
     * with typed helpers yet (Slack, GitHub, HubSpot, Jira, Gong, …).
     */
    public IntegrationCaller provider(String name) {
        return new IntegrationCaller(transport, name);
    }
}
