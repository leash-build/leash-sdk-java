package build.leash.sdk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LeashIntegrations client initialization, URL construction,
 * connect URL generation, and provider client access.
 */
class LeashIntegrationsTest {

    @Nested
    class ClientInitialization {

        @Test
        void defaultPlatformUrl() {
            assertEquals("https://leash.build", LeashIntegrations.DEFAULT_PLATFORM_URL);
        }

        @Test
        void buildWithAuthTokenOnly() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            assertNotNull(client);
        }

        @Test
        void buildWithApiKey() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .apiKey("my-api-key")
                    .build();
            assertNotNull(client);
        }

        @Test
        void buildWithCustomPlatformUrl() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .platformUrl("https://custom.example.com")
                    .authToken("test-token")
                    .build();
            assertNotNull(client);
        }

        @Test
        void buildWithNoTokens() {
            // Builder should not throw even with no tokens set
            LeashIntegrations client = LeashIntegrations.builder().build();
            assertNotNull(client);
        }

        @Test
        void buildWithCustomHttpClient() {
            java.net.http.HttpClient customClient = java.net.http.HttpClient.newHttpClient();
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .httpClient(customClient)
                    .build();
            assertNotNull(client);
        }
    }

    @Nested
    class ConnectUrlGeneration {

        @Test
        void connectUrlWithDefaultPlatformUrl() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();

            String url = client.getConnectUrl("gmail", null);
            assertEquals("https://leash.build/api/integrations/connect/gmail", url);
        }

        @Test
        void connectUrlWithCustomPlatformUrl() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .platformUrl("https://custom.example.com")
                    .authToken("test-token")
                    .build();

            String url = client.getConnectUrl("gmail", null);
            assertEquals("https://custom.example.com/api/integrations/connect/gmail", url);
        }

        @Test
        void connectUrlWithReturnUrl() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();

            String url = client.getConnectUrl("gmail", "https://myapp.com/callback");
            assertEquals(
                    "https://leash.build/api/integrations/connect/gmail?return_url=https%3A%2F%2Fmyapp.com%2Fcallback",
                    url);
        }

        @Test
        void connectUrlWithEmptyReturnUrl() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();

            String url = client.getConnectUrl("gmail", "");
            assertEquals("https://leash.build/api/integrations/connect/gmail", url);
        }

        @Test
        void connectUrlForDifferentProviders() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();

            assertEquals(
                    "https://leash.build/api/integrations/connect/google_calendar",
                    client.getConnectUrl("google_calendar", null));
            assertEquals(
                    "https://leash.build/api/integrations/connect/google_drive",
                    client.getConnectUrl("google_drive", null));
        }

        @Test
        void connectUrlTrailingSlashStripped() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .platformUrl("https://custom.example.com///")
                    .authToken("test-token")
                    .build();

            String url = client.getConnectUrl("gmail", null);
            assertEquals("https://custom.example.com/api/integrations/connect/gmail", url);
        }
    }

    @Nested
    class ProviderClients {

        @Test
        void gmailClientIsNotNull() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            GmailClient gmail = client.gmail();
            assertNotNull(gmail);
        }

        @Test
        void calendarClientIsNotNull() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            CalendarClient calendar = client.calendar();
            assertNotNull(calendar);
        }

        @Test
        void driveClientIsNotNull() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            DriveClient drive = client.drive();
            assertNotNull(drive);
        }

        @Test
        void customIntegrationIsNotNull() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            CustomIntegration integration = client.integration("stripe");
            assertNotNull(integration);
        }

        @Test
        void eachCallReturnsNewInstance() {
            LeashIntegrations client = LeashIntegrations.builder()
                    .authToken("test-token")
                    .build();
            GmailClient g1 = client.gmail();
            GmailClient g2 = client.gmail();
            assertNotSame(g1, g2);
        }
    }
}
