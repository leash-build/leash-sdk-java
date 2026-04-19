package build.leash.sdk;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LeashAuth server-side authentication.
 */
class LeashAuthTest {

    private static String createToken(String id, String email, String name, String picture) {
        return JWT.create()
                .withSubject(id)
                .withClaim("email", email)
                .withClaim("name", name)
                .withClaim("picture", picture)
                .sign(Algorithm.HMAC256("test-secret"));
    }

    private static String createUnsignedToken(String id, String email, String name, String picture) {
        return JWT.create()
                .withSubject(id)
                .withClaim("email", email)
                .withClaim("name", name)
                .withClaim("picture", picture)
                .sign(Algorithm.none());
    }

    @Test
    void validTokenReturnsUserWithCorrectFields() throws LeashError {
        String token = createUnsignedToken("user-123", "alice@example.com", "Alice", "https://img.example.com/alice.png");
        String cookie = "leash-auth=" + token;

        LeashUser user = LeashAuth.getUser(cookie);

        assertEquals("user-123", user.getId());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("Alice", user.getName());
        assertEquals("https://img.example.com/alice.png", user.getPicture());
    }

    @Test
    void missingCookieThrowsLeashError() {
        LeashError error = assertThrows(LeashError.class, () -> LeashAuth.getUser(null));
        assertEquals("auth_error", error.getCode());
    }

    @Test
    void emptyCookieHeaderThrowsLeashError() {
        LeashError error = assertThrows(LeashError.class, () -> LeashAuth.getUser(""));
        assertEquals("auth_error", error.getCode());
    }

    @Test
    void missingLeashAuthCookieThrowsLeashError() {
        LeashError error = assertThrows(LeashError.class, () -> LeashAuth.getUser("session=abc; theme=dark"));
        assertEquals("auth_error", error.getCode());
    }

    @Test
    void invalidTokenThrowsLeashError() {
        LeashError error = assertThrows(LeashError.class, () -> LeashAuth.getUser("leash-auth=not-a-jwt"));
        assertEquals("auth_error", error.getCode());
    }

    @Test
    void noSecretDecodesWithoutVerification() throws LeashError {
        // LEASH_JWT_SECRET is not set in test env, so this should decode without verification
        assertNull(LeashAuth.getSecret(), "LEASH_JWT_SECRET should not be set in test environment");

        String token = createUnsignedToken("u1", "bob@test.com", "Bob", "https://img.test.com/bob.png");
        String cookie = "leash-auth=" + token;

        LeashUser user = LeashAuth.getUser(cookie);
        assertEquals("u1", user.getId());
        assertEquals("bob@test.com", user.getEmail());
    }

    @Test
    void isAuthenticatedReturnsTrueForValidToken() {
        String token = createUnsignedToken("u1", "a@b.com", "A", "pic");
        assertTrue(LeashAuth.isAuthenticated("leash-auth=" + token));
    }

    @Test
    void isAuthenticatedReturnsFalseForMissingCookie() {
        assertFalse(LeashAuth.isAuthenticated(null));
    }

    @Test
    void isAuthenticatedReturnsFalseForInvalidToken() {
        assertFalse(LeashAuth.isAuthenticated("leash-auth=garbage"));
    }

    @Test
    void isAuthenticatedReturnsFalseForWrongCookie() {
        assertFalse(LeashAuth.isAuthenticated("other=value"));
    }

    @Test
    void getUserFromTokenWorksWithRawToken() throws LeashError {
        String token = createUnsignedToken("u99", "raw@test.com", "Raw User", "https://pic.test/raw.png");

        LeashUser user = LeashAuth.getUserFromToken(token);

        assertEquals("u99", user.getId());
        assertEquals("raw@test.com", user.getEmail());
        assertEquals("Raw User", user.getName());
        assertEquals("https://pic.test/raw.png", user.getPicture());
    }

    @Test
    void getUserFromTokenThrowsOnNull() {
        LeashError error = assertThrows(LeashError.class, () -> LeashAuth.getUserFromToken(null));
        assertEquals("auth_error", error.getCode());
    }

    @Test
    void getUserFromTokenThrowsOnEmpty() {
        LeashError error = assertThrows(LeashError.class, () -> LeashAuth.getUserFromToken(""));
        assertEquals("auth_error", error.getCode());
    }

    @Test
    void cookieParsingMultipleCookies() throws LeashError {
        String token = createUnsignedToken("u2", "multi@test.com", "Multi", "pic");
        String cookie = "session=xyz; leash-auth=" + token + "; theme=dark";

        LeashUser user = LeashAuth.getUser(cookie);
        assertEquals("u2", user.getId());
        assertEquals("multi@test.com", user.getEmail());
    }

    @Test
    void cookieParsingLeashAuthInMiddle() throws LeashError {
        String token = createUnsignedToken("mid", "mid@test.com", "Mid", "pic");
        String cookie = "a=1; b=2; leash-auth=" + token + "; c=3; d=4";

        LeashUser user = LeashAuth.getUser(cookie);
        assertEquals("mid", user.getId());
    }

    @Test
    void cookieParsingUrlEncodedValue() throws LeashError {
        String token = createUnsignedToken("enc", "enc@test.com", "Encoded", "pic");
        String encoded = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String cookie = "leash-auth=" + encoded;

        LeashUser user = LeashAuth.getUser(cookie);
        assertEquals("enc", user.getId());
        assertEquals("enc@test.com", user.getEmail());
    }

    @Test
    void cookieParsingWithSpacesAroundEquals() throws LeashError {
        String token = createUnsignedToken("sp", "sp@test.com", "Spaced", "pic");
        // Some user agents may have spaces; extractCookie trims
        String cookie = "foo=bar; leash-auth=" + token + " ; baz=qux";

        LeashUser user = LeashAuth.getUser(cookie);
        assertEquals("sp", user.getId());
    }
}
