package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/** A Linear issue. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class LinearIssue {

    private final String id;
    private final String identifier;
    private final String title;
    private final String description;
    private final Integer priority;
    private final String createdAt;
    private final String updatedAt;
    private final String url;
    private final LinearUserRef assignee;
    private final LinearStateRef state;
    private final LinearTeamRef team;
    private final List<String> labelIds;
    private final String projectId;

    public LinearIssue(
            @JsonProperty("id") String id,
            @JsonProperty("identifier") String identifier,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("priority") Integer priority,
            @JsonProperty("createdAt") String createdAt,
            @JsonProperty("updatedAt") String updatedAt,
            @JsonProperty("url") String url,
            @JsonProperty("assignee") LinearUserRef assignee,
            @JsonProperty("state") LinearStateRef state,
            @JsonProperty("team") LinearTeamRef team,
            @JsonProperty("labelIds") List<String> labelIds,
            @JsonProperty("projectId") String projectId) {
        this.id = id;
        this.identifier = identifier;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.url = url;
        this.assignee = assignee;
        this.state = state;
        this.team = team;
        this.labelIds = labelIds == null ? Collections.emptyList() : List.copyOf(labelIds);
        this.projectId = projectId;
    }

    public String getId() { return id; }
    public String getIdentifier() { return identifier; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getPriority() { return priority; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getUrl() { return url; }
    public LinearUserRef getAssignee() { return assignee; }
    public LinearStateRef getState() { return state; }
    public LinearTeamRef getTeam() { return team; }
    public List<String> getLabelIds() { return labelIds; }
    public String getProjectId() { return projectId; }
}
