package build.leash.integrations.drive;

import build.leash.internal.Json;
import build.leash.internal.Transport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * Typed Google Drive provider client (platform id {@code google_drive}).
 * Exposes the 7 verbs the TS surface has: {@code listFiles}, {@code getFile},
 * {@code downloadFile}, {@code createFolder}, {@code uploadFile},
 * {@code deleteFile}, {@code searchFiles}.
 */
public final class DriveIntegration {

    private static final String PROVIDER = "google_drive";

    private final Transport transport;

    public DriveIntegration(Transport transport) {
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    public DriveFileList listFiles() {
        return listFiles(null);
    }

    public DriveFileList listFiles(DriveListFilesParams params) {
        JsonNode raw = transport.integrationsCall(PROVIDER, "list-files", params);
        return Json.treeToValue(raw, DriveFileList.class);
    }

    public DriveFile getFile(String fileId) {
        Objects.requireNonNull(fileId, "fileId");
        ObjectNode body = Json.newObject();
        body.put("fileId", fileId);
        JsonNode raw = transport.integrationsCall(PROVIDER, "get-file", body);
        return Json.treeToValue(raw, DriveFile.class);
    }

    /**
     * Returns the platform's raw {@code download-file} response envelope.
     * The shape depends on the file type (base64 for binaries, plain text
     * for text files), so the SDK surfaces it as a JsonNode.
     */
    public JsonNode downloadFile(String fileId) {
        Objects.requireNonNull(fileId, "fileId");
        ObjectNode body = Json.newObject();
        body.put("fileId", fileId);
        return transport.integrationsCall(PROVIDER, "download-file", body);
    }

    public DriveFile createFolder(String name) {
        return createFolder(name, null);
    }

    public DriveFile createFolder(String name, String parentId) {
        Objects.requireNonNull(name, "name");
        ObjectNode body = Json.newObject();
        body.put("name", name);
        if (parentId != null && !parentId.isEmpty()) {
            body.put("parentId", parentId);
        }
        JsonNode raw = transport.integrationsCall(PROVIDER, "create-folder", body);
        return Json.treeToValue(raw, DriveFile.class);
    }

    public DriveFile uploadFile(DriveUploadFileParams params) {
        Objects.requireNonNull(params, "params");
        JsonNode raw = transport.integrationsCall(PROVIDER, "upload-file", params);
        return Json.treeToValue(raw, DriveFile.class);
    }

    public JsonNode deleteFile(String fileId) {
        Objects.requireNonNull(fileId, "fileId");
        ObjectNode body = Json.newObject();
        body.put("fileId", fileId);
        return transport.integrationsCall(PROVIDER, "delete-file", body);
    }

    public DriveFileList searchFiles(String query) {
        return searchFiles(query, null);
    }

    public DriveFileList searchFiles(String query, Integer maxResults) {
        Objects.requireNonNull(query, "query");
        ObjectNode body = Json.newObject();
        body.put("query", query);
        if (maxResults != null) {
            body.put("maxResults", maxResults);
        }
        JsonNode raw = transport.integrationsCall(PROVIDER, "search-files", body);
        return Json.treeToValue(raw, DriveFileList.class);
    }
}
