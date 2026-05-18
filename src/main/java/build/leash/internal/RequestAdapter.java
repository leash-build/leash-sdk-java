package build.leash.internal;

import java.util.Optional;

/**
 * Framework-agnostic view of an inbound HTTP request — enough surface for
 * the SDK to extract the {@code leash-auth} cookie and the
 * {@code Authorization: Bearer …} header.
 *
 * <p>Concrete implementations live in {@code build.leash} (e.g. the
 * Servlet adapter and the plain-map adapter). The {@link build.leash.Leash}
 * constructor inspects the incoming object and picks one — see
 * {@code build.leash.RequestAdapters}.
 */
public interface RequestAdapter {

    /** Read a cookie by name. Returns empty when no cookie matches. */
    Optional<String> getCookie(String name);

    /**
     * Case-insensitive header read. Returns empty when no header matches.
     */
    Optional<String> getHeader(String name);
}
