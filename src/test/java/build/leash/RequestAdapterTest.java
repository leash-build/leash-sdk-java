package build.leash;

import build.leash.internal.AuthExtractor;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestAdapterTest {

    @Test
    void headerMap_caseInsensitiveHeaderLookup() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer xyz");
        HeaderMapRequestAdapter a = new HeaderMapRequestAdapter(headers, Collections.emptyMap());
        assertEquals("Bearer xyz", a.getHeader("authorization").orElseThrow());
        assertEquals("Bearer xyz", a.getHeader("AUTHORIZATION").orElseThrow());
    }

    @Test
    void headerMap_directCookieMapWins() {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("leash-auth", "direct");
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "leash-auth=fallback");
        HeaderMapRequestAdapter a = new HeaderMapRequestAdapter(headers, cookies);
        assertEquals("direct", a.getCookie("leash-auth").orElseThrow());
    }

    @Test
    void headerMap_fallsBackToCookieHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("cookie", "other=1; leash-auth=from-header; more=2");
        HeaderMapRequestAdapter a = new HeaderMapRequestAdapter(headers, Collections.emptyMap());
        assertEquals("from-header", a.getCookie("leash-auth").orElseThrow());
    }

    @Test
    void headerMap_missingHeaderReturnsEmpty() {
        HeaderMapRequestAdapter a = new HeaderMapRequestAdapter(Collections.emptyMap(), Collections.emptyMap());
        assertTrue(a.getHeader("anything").isEmpty());
        assertTrue(a.getCookie("anything").isEmpty());
    }

    @Test
    void extractBearerToken_extractsToken() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer my-jwt-here");
        HeaderMapRequestAdapter a = new HeaderMapRequestAdapter(headers, Collections.emptyMap());
        Optional<String> token = AuthExtractor.extractBearerToken(a);
        assertEquals("my-jwt-here", token.orElseThrow());
    }

    @Test
    void extractBearerToken_caseInsensitiveScheme() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer lower-case");
        HeaderMapRequestAdapter a = new HeaderMapRequestAdapter(headers, Collections.emptyMap());
        assertEquals("lower-case", AuthExtractor.extractBearerToken(a).orElseThrow());
    }

    @Test
    void extractBearerToken_skipsNonBearer() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic abc");
        HeaderMapRequestAdapter a = new HeaderMapRequestAdapter(headers, Collections.emptyMap());
        assertTrue(AuthExtractor.extractBearerToken(a).isEmpty());
    }

    @Test
    void parseCookieValue_handlesTrailingSemicolon() {
        Optional<String> v = AuthExtractor.parseCookieValue("a=1; leash-auth=xyz;", "leash-auth");
        assertEquals("xyz", v.orElseThrow());
    }

    @Test
    void parseCookieValue_missingReturnsEmpty() {
        Optional<String> v = AuthExtractor.parseCookieValue("a=1; b=2", "leash-auth");
        assertTrue(v.isEmpty());
    }
}
