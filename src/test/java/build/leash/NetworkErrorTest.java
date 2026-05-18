package build.leash;

import build.leash.errors.NetworkException;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

class NetworkErrorTest {

    @Test
    void unreachablePlatform_throwsNetworkException() {
        // Point at a port nothing is listening on — connection refused.
        Leash leash = Leash.builder()
                .apiKey("lsk_live_test")
                .platformUrl("http://127.0.0.1:1")
                .httpClient(TestSupport.http())
                .request(Collections.emptyMap(), Collections.emptyMap())
                .build();
        assertThrows(NetworkException.class, () -> leash.env().get("ANY"));
    }
}
