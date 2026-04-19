package build.leash.sdk;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Framework-agnostic server-side authentication for Leash.
 *
 * <p>Works with any Java web framework — just pass the raw Cookie header string:
 * <pre>{@code
 * // Spring
 * LeashUser user = LeashAuth.getUser(request.getHeader("Cookie"));
 *
 * // Jakarta Servlet
 * LeashUser user = LeashAuth.getUser(request.getHeader("Cookie"));
 *
 * // Javalin
 * LeashUser user = LeashAuth.getUser(ctx.header("Cookie"));
 * }</pre>
 *
 * <p>If the {@code LEASH_JWT_SECRET} environment variable is set, tokens are
 * verified using HMAC-SHA256. Otherwise tokens are decoded without verification
 * (suitable for development or when the platform is trusted).
 */
public final class LeashAuth {

    private static final String COOKIE_NAME = "leash-auth";

    private LeashAuth() {}

    /**
     * Extracts and decodes the authenticated user from a raw Cookie header.
     *
     * @param cookieHeader the value of the HTTP Cookie header
     * @return the authenticated LeashUser
     * @throws LeashError if the cookie is missing or the token is invalid
     */
    public static LeashUser getUser(String cookieHeader) throws LeashError {
        String token = extractCookie(cookieHeader, COOKIE_NAME);
        return getUserFromToken(token);
    }

    /**
     * Decodes a raw JWT token into a LeashUser.
     *
     * <p>If {@code LEASH_JWT_SECRET} is set, the token is verified with HMAC-SHA256.
     * Otherwise the token is decoded without signature verification.
     *
     * @param token the raw JWT token string
     * @return the authenticated LeashUser
     * @throws LeashError if the token is null, empty, or invalid
     */
    public static LeashUser getUserFromToken(String token) throws LeashError {
        if (token == null || token.isEmpty()) {
            throw new LeashError("Missing auth token", "auth_error");
        }

        try {
            DecodedJWT jwt = decodeOrVerify(token);

            String id = jwt.getSubject();
            String email = jwt.getClaim("email").asString();
            String name = jwt.getClaim("name").asString();
            String picture = jwt.getClaim("picture").asString();

            return new LeashUser(id, email, name, picture);
        } catch (JWTDecodeException e) {
            throw new LeashError("Invalid auth token: " + e.getMessage(), "auth_error");
        } catch (JWTVerificationException e) {
            throw new LeashError("Auth token verification failed: " + e.getMessage(), "auth_error");
        }
    }

    /**
     * Checks whether the Cookie header contains a valid {@code leash-auth} token.
     *
     * @param cookieHeader the value of the HTTP Cookie header
     * @return true if a valid leash-auth token is present
     */
    public static boolean isAuthenticated(String cookieHeader) {
        try {
            getUser(cookieHeader);
            return true;
        } catch (LeashError e) {
            return false;
        }
    }

    // --- Internal helpers ---

    static String getSecret() {
        return System.getenv("LEASH_JWT_SECRET");
    }

    private static DecodedJWT decodeOrVerify(String token) {
        String secret = getSecret();
        if (secret != null && !secret.isEmpty()) {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
        }
        return JWT.decode(token);
    }

    static String extractCookie(String cookieHeader, String name) throws LeashError {
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            throw new LeashError("Missing cookie header", "auth_error");
        }

        String[] pairs = cookieHeader.split(";");
        for (String pair : pairs) {
            String trimmed = pair.trim();
            int eq = trimmed.indexOf('=');
            if (eq < 0) continue;
            String cookieName = trimmed.substring(0, eq).trim();
            if (name.equals(cookieName)) {
                String value = trimmed.substring(eq + 1).trim();
                return URLDecoder.decode(value, StandardCharsets.UTF_8);
            }
        }

        throw new LeashError("Missing leash-auth cookie", "auth_error");
    }
}
