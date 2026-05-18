package build.leash;

import build.leash.internal.JwtDecoder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtDecoderTest {

    private static String jwt(Map<String, Object> payload) {
        String header = b64u("{\"alg\":\"none\",\"typ\":\"JWT\"}");
        String body = b64u(toJson(payload));
        return header + "." + body + ".signature";
    }

    private static String b64u(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String toJson(Map<String, Object> payload) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void decode_extractsUserClaims() {
        long future = System.currentTimeMillis() / 1000L + 3600;
        String token = jwt(Map.of(
                "userId", "u-1",
                "email", "x@y.com",
                "name", "Test User",
                "picture", "https://example.com/p.png",
                "exp", future));

        LeashUser user = JwtDecoder.decodeToUser(token);
        assertEquals("u-1", user.getId());
        assertEquals("x@y.com", user.getEmail());
        assertEquals("Test User", user.getName());
        assertTrue(user.getPicture().isPresent());
    }

    @Test
    void decode_fallsBackToSub_whenNoUserId() {
        long future = System.currentTimeMillis() / 1000L + 3600;
        String token = jwt(Map.of("sub", "sub-1", "exp", future));
        LeashUser user = JwtDecoder.decodeToUser(token);
        assertEquals("sub-1", user.getId());
    }

    @Test
    void decode_rejectsExpired() {
        long past = System.currentTimeMillis() / 1000L - 60;
        String token = jwt(Map.of("userId", "u-1", "exp", past));
        assertThrows(Exception.class, () -> JwtDecoder.decodeToUser(token));
    }

    @Test
    void decode_rejectsMalformed() {
        assertThrows(Exception.class, () -> JwtDecoder.decodeToUser("not-a-jwt"));
    }

    @Test
    void decode_emptyTokenThrows() {
        assertThrows(Exception.class, () -> JwtDecoder.decodeToUser(""));
    }

    @Test
    void auth_userReturnsEmpty_forInvalidJwt() {
        Leash leash = Leash.fromToken("bogus");
        assertTrue(leash.auth().user().isEmpty());
    }

    @Test
    void auth_userReturnsLeashUser_forValidJwt() {
        long future = System.currentTimeMillis() / 1000L + 3600;
        String token = jwt(Map.of("userId", "u-99", "email", "z@y.com", "exp", future));
        Leash leash = Leash.fromToken(token);
        assertTrue(leash.auth().user().isPresent());
        assertEquals("u-99", leash.auth().user().get().getId());
    }

    @Test
    void auth_isAuthenticated_mirrorsUserPresence() {
        long future = System.currentTimeMillis() / 1000L + 3600;
        String good = jwt(Map.of("userId", "u-1", "exp", future));
        Leash authed = Leash.fromToken(good);
        Leash noauth = Leash.fromToken("nope");
        assertTrue(authed.auth().isAuthenticated());
        assertTrue(!noauth.auth().isAuthenticated());
    }
}
