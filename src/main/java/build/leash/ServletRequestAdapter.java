package build.leash;

import build.leash.internal.RequestAdapter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * Adapts a Jakarta {@link HttpServletRequest} for cookie + header reads.
 *
 * <p>Only resolves at runtime when the consumer brings their own
 * {@code jakarta.servlet:jakarta.servlet-api} on the classpath — the SDK's
 * Maven dep is marked {@code provided}/{@code optional}. Spring Boot,
 * Jakarta EE, Jetty, Tomcat, and Quarkus all ship the API.
 */
public final class ServletRequestAdapter implements RequestAdapter {

    private final HttpServletRequest request;

    public ServletRequestAdapter(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Optional<String> getCookie(String name) {
        if (request == null) return Optional.empty();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (name.equals(c.getName())) {
                    return Optional.ofNullable(c.getValue());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getHeader(String name) {
        if (request == null || name == null) return Optional.empty();
        return Optional.ofNullable(request.getHeader(name));
    }
}
