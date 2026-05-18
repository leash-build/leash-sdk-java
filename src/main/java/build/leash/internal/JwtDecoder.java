package build.leash.internal;

import build.leash.LeashUser;
import build.leash.errors.LeashErrorCode;
import build.leash.errors.LeashException;
import com.fasterxml.jackson.databind.JsonNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Minimal stdlib-only JWT decoder. Mirrors the dev-fallback in
 * {@code leash-sdk-ts/src/server/auth.ts}'s {@code decodeToken} and
 * {@code leash-sdk-python/leash/auth.py}'s {@code decode_token}.
 *
 * <p>Verifies HS256 signatures when {@code LEASH_JWT_SECRET} is set;
 * otherwise decodes without verification so local development works
 * without provisioning a secret. Expiry is always enforced.
 */
public final class JwtDecoder {

    private JwtDecoder() {}

    public static LeashUser decodeToUser(String token) {
        return decodeToUser(token, System.getenv("LEASH_JWT_SECRET"));
    }

    static LeashUser decodeToUser(String token, String secret) {
        if (token == null || token.isEmpty()) {
            throw new LeashException(
                    LeashErrorCode.NO_AUTH_CONTEXT,
                    "Empty leash-auth cookie value.");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new LeashException(
                    LeashErrorCode.NO_AUTH_CONTEXT,
                    "Invalid leash-auth cookie: malformed JWT.");
        }
        if (secret != null && !secret.isEmpty()) {
            verifyHs256(parts[0] + "." + parts[1], parts[2], secret);
        }
        JsonNode payload = decodeSegment(parts[1]);
        if (payload == null || !payload.isObject()) {
            throw new LeashException(
                    LeashErrorCode.NO_AUTH_CONTEXT,
                    "Invalid leash-auth cookie: payload is not a JSON object.");
        }
        JsonNode exp = payload.get("exp");
        if (exp != null && exp.canConvertToLong()) {
            long expSec = exp.asLong();
            if (expSec > 0 && (System.currentTimeMillis() / 1000L) > expSec) {
                throw new LeashException(
                        LeashErrorCode.NO_AUTH_CONTEXT,
                        "leash-auth cookie has expired.");
            }
        }
        String id = textOrEmpty(payload, "userId");
        if (id.isEmpty()) id = textOrEmpty(payload, "sub");
        if (id.isEmpty()) {
            throw new LeashException(
                    LeashErrorCode.NO_AUTH_CONTEXT,
                    "leash-auth cookie missing user identifier.");
        }
        String picture = textOrEmpty(payload, "picture");
        return LeashUser.builder()
                .id(id)
                .email(textOrEmpty(payload, "email"))
                .name(textOrEmpty(payload, "name"))
                .picture(picture.isEmpty() ? null : picture)
                .build();
    }

    private static String textOrEmpty(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? "" : v.asText("");
    }

    private static JsonNode decodeSegment(String segment) {
        try {
            byte[] raw = Base64.getUrlDecoder().decode(segment);
            return Json.readTree(new String(raw, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void verifyHs256(String signed, String sigB64, String secret) {
        try {
            byte[] expected = Base64.getUrlDecoder().decode(sigB64);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] got = mac.doFinal(signed.getBytes(StandardCharsets.UTF_8));
            if (!constantTimeEquals(expected, got)) {
                throw new LeashException(
                        LeashErrorCode.NO_AUTH_CONTEXT,
                        "Invalid leash-auth cookie: signature mismatch.");
            }
        } catch (LeashException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw new LeashException(
                    LeashErrorCode.NO_AUTH_CONTEXT,
                    "Invalid leash-auth cookie: HS256 verification failed.", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
