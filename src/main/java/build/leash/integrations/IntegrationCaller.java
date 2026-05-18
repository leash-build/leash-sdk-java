package build.leash.integrations;

import build.leash.internal.Transport;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * Generic provider invoker — returned by
 * {@link IntegrationsNamespace#provider(String)}.
 *
 * <p>No typed shape; just a pass-through of action name + params. Used for
 * providers the SDK doesn't have typed wrappers for yet.
 */
public final class IntegrationCaller {

    private final Transport transport;
    private final String name;

    IntegrationCaller(Transport transport, String name) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.name = Objects.requireNonNull(name, "name");
    }

    /** The provider id this caller is bound to (e.g. {@code "slack"}). */
    public String name() {
        return name;
    }

    /** POST {@code /api/integrations/{name}/{action}} with the given body. */
    public JsonNode call(String action, Object params) {
        return transport.integrationsCall(name, action, params);
    }
}
