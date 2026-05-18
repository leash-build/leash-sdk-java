package build.leash.integrations.drive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** Body for {@code Drive.uploadFile(...)}. {@code name}, {@code content}, {@code mimeType} are required. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class DriveUploadFileParams {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("content")
    private final String content;

    @JsonProperty("mimeType")
    private final String mimeType;

    @JsonProperty("parentId")
    private final String parentId;

    private DriveUploadFileParams(Builder b) {
        this.name = Objects.requireNonNull(b.name, "name");
        this.content = Objects.requireNonNull(b.content, "content");
        this.mimeType = Objects.requireNonNull(b.mimeType, "mimeType");
        this.parentId = b.parentId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() { return name; }
    public String getContent() { return content; }
    public String getMimeType() { return mimeType; }
    public String getParentId() { return parentId; }

    public static final class Builder {
        private String name;
        private String content;
        private String mimeType;
        private String parentId;

        public Builder name(String name) { this.name = name; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public Builder parentId(String parentId) { this.parentId = parentId; return this; }

        public DriveUploadFileParams build() {
            return new DriveUploadFileParams(this);
        }
    }
}
