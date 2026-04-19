package build.leash.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LeashError exception class.
 */
class LeashErrorTest {

    @Test
    void messageFormattedWithCode() {
        LeashError error = new LeashError("Not connected", "not_connected");
        assertEquals("leash: Not connected (code: not_connected)", error.getMessage());
        assertEquals("leash: Not connected (code: not_connected)", error.getErrorMessage());
    }

    @Test
    void messageFormattedWithoutCode() {
        LeashError error = new LeashError("Something went wrong", null);
        assertEquals("leash: Something went wrong", error.getMessage());
    }

    @Test
    void messageFormattedWithEmptyCode() {
        LeashError error = new LeashError("Something went wrong", "");
        assertEquals("leash: Something went wrong", error.getMessage());
    }

    @Test
    void codeAccessor() {
        LeashError error = new LeashError("err", "my_code");
        assertEquals("my_code", error.getCode());
    }

    @Test
    void connectUrlNull() {
        LeashError error = new LeashError("err", "code");
        assertNull(error.getConnectUrl());
    }

    @Test
    void connectUrlPresent() {
        LeashError error = new LeashError("Not connected", "not_connected",
                "https://leash.build/connect/gmail");
        assertEquals("https://leash.build/connect/gmail", error.getConnectUrl());
    }

    @Test
    void isException() {
        LeashError error = new LeashError("err", "code");
        assertInstanceOf(Exception.class, error);
    }

    @Test
    void canBeCaught() {
        assertThrows(LeashError.class, () -> {
            throw new LeashError("test error", "test_code");
        });
    }

    @Test
    void threeArgConstructor() {
        LeashError error = new LeashError("msg", "code", "https://example.com");
        assertEquals("leash: msg (code: code)", error.getMessage());
        assertEquals("code", error.getCode());
        assertEquals("https://example.com", error.getConnectUrl());
    }
}
