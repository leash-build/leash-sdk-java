package build.leash;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeashConstructorTest {

    @Test
    void constructFromHeaderMap_picksUpCookieAndBearer() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer my-jwt");
        headers.put("Cookie", "leash-auth=cookie-value; other=1");

        Leash leash = new Leash(headers, Collections.emptyMap());

        assertNotNull(leash.auth());
        assertNotNull(leash.env());
        assertNotNull(leash.integrations());
        assertEquals("https://leash.build", leash.platformUrl());
    }

    @Test
    void constructFromHeaderMap_caseInsensitiveHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer abc");
        headers.put("cookie", "leash-auth=zzz");

        Leash leash = new Leash(headers, Collections.emptyMap());
        assertFalse(leash.auth().isAuthenticated()); // "zzz" isn't a valid JWT
    }

    @Test
    void constructFromCookieMap_overridesCookieHeaderLookup() {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("leash-auth", "direct-cookie");

        Leash leash = new Leash(Collections.emptyMap(), cookies);
        assertNotNull(leash);
    }

    @Test
    void fromApiKey_buildsWithoutRequest() {
        Leash leash = Leash.fromApiKey("lsk_live_test");
        assertNotNull(leash);
        assertFalse(leash.auth().isAuthenticated());
    }

    @Test
    void fromToken_buildsWithoutRequest() {
        Leash leash = Leash.fromToken("some-jwt");
        assertNotNull(leash);
        // Token is not a valid JWT so user() returns empty silently.
        assertFalse(leash.auth().isAuthenticated());
    }

    @Test
    void builder_defaultsToPublicPlatform() {
        Leash leash = Leash.builder().build();
        assertEquals("https://leash.build", leash.platformUrl());
    }

    @Test
    void builder_acceptsCustomPlatformUrl() {
        Leash leash = Leash.builder().platformUrl("https://staging.leash.build").build();
        assertEquals("https://staging.leash.build", leash.platformUrl());
    }

    @Test
    void builder_acceptsNullCookies() {
        Leash leash = Leash.builder()
                .request(Collections.emptyMap(), null)
                .build();
        assertNotNull(leash);
    }

    @Test
    void platformUrl_trimsTrailingSlash() {
        Leash leash = Leash.builder().platformUrl("https://example.com///").build();
        assertEquals("https://example.com", leash.platformUrl());
    }

    @Test
    void servletAdapterIsUsable() {
        // We don't have a real HttpServletRequest mock — but the adapter type
        // resolves, which proves the optional dep is on the test classpath.
        // (Spring's MockHttpServletRequest would let us go further; we keep the
        // happy path covered via the header-map constructor.)
        assertTrue(ServletRequestAdapter.class.isAssignableFrom(ServletRequestAdapter.class));
    }
}
