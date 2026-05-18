package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Response from {@code Linear.listIssues(...)}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class LinearListIssuesResult {

    private final List<LinearIssue> issues;
    private final String cursor;

    public LinearListIssuesResult(
            @JsonProperty("issues") List<LinearIssue> issues,
            @JsonProperty("cursor") String cursor) {
        this.issues = issues == null ? Collections.emptyList() : List.copyOf(issues);
        this.cursor = cursor;
    }

    public List<LinearIssue> getIssues() {
        return issues;
    }

    public Optional<String> getCursor() {
        return Optional.ofNullable(cursor);
    }
}
