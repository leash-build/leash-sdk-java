package build.leash.integrations.drive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Response from {@code Drive.listFiles(...)} / {@code Drive.searchFiles(...)}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DriveFileList {

    private final List<DriveFile> files;
    private final String nextPageToken;

    public DriveFileList(
            @JsonProperty("files") List<DriveFile> files,
            @JsonProperty("nextPageToken") String nextPageToken) {
        this.files = files == null ? Collections.emptyList() : List.copyOf(files);
        this.nextPageToken = nextPageToken;
    }

    public List<DriveFile> getFiles() {
        return files;
    }

    public Optional<String> getNextPageToken() {
        return Optional.ofNullable(nextPageToken);
    }
}
