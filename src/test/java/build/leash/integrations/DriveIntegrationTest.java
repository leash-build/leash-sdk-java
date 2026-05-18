package build.leash.integrations;

import build.leash.Leash;
import build.leash.TestSupport;
import build.leash.integrations.drive.DriveFile;
import build.leash.integrations.drive.DriveFileList;
import build.leash.integrations.drive.DriveListFilesParams;
import build.leash.integrations.drive.DriveUploadFileParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DriveIntegrationTest {

    private WireMockServer wm;
    private Leash leash;

    @BeforeEach
    void setUp() {
        wm = TestSupport.startWireMock();
        leash = TestSupport.leashFor(wm, "lsk_live_test");
    }

    @AfterEach
    void tearDown() {
        if (wm != null) wm.stop();
    }

    @Test
    void listFiles_defaults() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_drive/list-files"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"files\":[{\"id\":\"f1\",\"name\":\"a.txt\",\"mimeType\":\"text/plain\"}]}")));

        DriveFileList list = leash.integrations().drive().listFiles();
        assertEquals(1, list.getFiles().size());
        assertEquals("a.txt", list.getFiles().get(0).getName());
    }

    @Test
    void listFiles_withParams() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_drive/list-files"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"files\":[]}")));

        leash.integrations().drive().listFiles(
                DriveListFilesParams.builder()
                        .query("name contains 'report'")
                        .maxResults(10)
                        .build());

        verify(postRequestedFor(urlEqualTo("/api/integrations/google_drive/list-files"))
                .withRequestBody(equalToJson(
                        "{\"query\":\"name contains 'report'\",\"maxResults\":10}")));
    }

    @Test
    void getFile_sendsId() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_drive/get-file"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"f1\",\"name\":\"a.txt\",\"mimeType\":\"text/plain\"}")));

        DriveFile file = leash.integrations().drive().getFile("f1");
        assertEquals("f1", file.getId());

        verify(postRequestedFor(urlEqualTo("/api/integrations/google_drive/get-file"))
                .withRequestBody(equalToJson("{\"fileId\":\"f1\"}")));
    }

    @Test
    void downloadFile_returnsRawNode() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_drive/download-file"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"content\":\"aGVsbG8=\",\"mimeType\":\"text/plain\"}")));

        JsonNode node = leash.integrations().drive().downloadFile("f1");
        assertEquals("aGVsbG8=", node.get("content").asText());
    }

    @Test
    void createFolder_withoutParent() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_drive/create-folder"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"f2\",\"name\":\"My Folder\",\"mimeType\":\"application/vnd.google-apps.folder\"}")));

        leash.integrations().drive().createFolder("My Folder");

        verify(postRequestedFor(urlEqualTo("/api/integrations/google_drive/create-folder"))
                .withRequestBody(equalToJson("{\"name\":\"My Folder\"}")));
    }

    @Test
    void createFolder_withParent() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_drive/create-folder"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"f3\",\"name\":\"Sub\",\"mimeType\":\"application/vnd.google-apps.folder\"}")));

        leash.integrations().drive().createFolder("Sub", "parent-id");

        verify(postRequestedFor(urlEqualTo("/api/integrations/google_drive/create-folder"))
                .withRequestBody(equalToJson("{\"name\":\"Sub\",\"parentId\":\"parent-id\"}")));
    }

    @Test
    void uploadFile_serialisesBody() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_drive/upload-file"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"f4\",\"name\":\"a.txt\",\"mimeType\":\"text/plain\"}")));

        DriveFile uploaded = leash.integrations().drive().uploadFile(
                DriveUploadFileParams.builder()
                        .name("a.txt").content("hi").mimeType("text/plain").build());
        assertEquals("f4", uploaded.getId());

        verify(postRequestedFor(urlEqualTo("/api/integrations/google_drive/upload-file"))
                .withRequestBody(equalToJson(
                        "{\"name\":\"a.txt\",\"content\":\"hi\",\"mimeType\":\"text/plain\"}")));
    }

    @Test
    void deleteFile_returnsRawNode() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_drive/delete-file"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"deleted\":true}")));

        JsonNode node = leash.integrations().drive().deleteFile("f1");
        assertNotNull(node);
        assertEquals(true, node.get("deleted").asBoolean());
    }

    @Test
    void searchFiles_withMaxResults() {
        wm.stubFor(post(urlEqualTo("/api/integrations/google_drive/search-files"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"files\":[]}")));

        leash.integrations().drive().searchFiles("name contains 'Q4'", 25);

        verify(postRequestedFor(urlEqualTo("/api/integrations/google_drive/search-files"))
                .withRequestBody(equalToJson(
                        "{\"query\":\"name contains 'Q4'\",\"maxResults\":25}")));
    }

    @Test
    void googleDrive_aliasReturnsSameInstance() {
        assertSame(leash.integrations().drive(), leash.integrations().googleDrive());
    }
}
