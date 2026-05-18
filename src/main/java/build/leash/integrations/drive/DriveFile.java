package build.leash.integrations.drive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/** Metadata view of a Google Drive file. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DriveFile {

    private final String id;
    private final String name;
    private final String mimeType;
    private final String size;
    private final String createdTime;
    private final String modifiedTime;
    private final List<String> parents;
    private final String webViewLink;
    private final String webContentLink;

    public DriveFile(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("mimeType") String mimeType,
            @JsonProperty("size") String size,
            @JsonProperty("createdTime") String createdTime,
            @JsonProperty("modifiedTime") String modifiedTime,
            @JsonProperty("parents") List<String> parents,
            @JsonProperty("webViewLink") String webViewLink,
            @JsonProperty("webContentLink") String webContentLink) {
        this.id = id;
        this.name = name;
        this.mimeType = mimeType;
        this.size = size;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.parents = parents == null ? Collections.emptyList() : List.copyOf(parents);
        this.webViewLink = webViewLink;
        this.webContentLink = webContentLink;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMimeType() { return mimeType; }
    public String getSize() { return size; }
    public String getCreatedTime() { return createdTime; }
    public String getModifiedTime() { return modifiedTime; }
    public List<String> getParents() { return parents; }
    public String getWebViewLink() { return webViewLink; }
    public String getWebContentLink() { return webContentLink; }
}
