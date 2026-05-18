package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/** Body for {@code Linear.createIssue(...)}. {@code teamId} and {@code title} are required. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LinearCreateIssueInput {

    @JsonProperty("teamId")
    private final String teamId;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("assigneeId")
    private final String assigneeId;

    @JsonProperty("priority")
    private final Integer priority;

    @JsonProperty("labelIds")
    private final List<String> labelIds;

    private LinearCreateIssueInput(Builder b) {
        this.teamId = Objects.requireNonNull(b.teamId, "teamId");
        this.title = Objects.requireNonNull(b.title, "title");
        this.description = b.description;
        this.assigneeId = b.assigneeId;
        this.priority = b.priority;
        this.labelIds = b.labelIds == null ? null : List.copyOf(b.labelIds);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTeamId() { return teamId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAssigneeId() { return assigneeId; }
    public Integer getPriority() { return priority; }
    public List<String> getLabelIds() { return labelIds; }

    public static final class Builder {
        private String teamId;
        private String title;
        private String description;
        private String assigneeId;
        private Integer priority;
        private List<String> labelIds;

        public Builder teamId(String teamId) { this.teamId = teamId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder assigneeId(String assigneeId) { this.assigneeId = assigneeId; return this; }
        public Builder priority(Integer priority) { this.priority = priority; return this; }
        public Builder labelIds(List<String> labelIds) { this.labelIds = labelIds; return this; }

        public LinearCreateIssueInput build() {
            return new LinearCreateIssueInput(this);
        }
    }
}
