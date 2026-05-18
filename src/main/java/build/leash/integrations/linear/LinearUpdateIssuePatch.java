package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Partial-update body for {@code Linear.updateIssue(...)}. All fields optional. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LinearUpdateIssuePatch {

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

    private LinearUpdateIssuePatch(Builder b) {
        this.teamId = b.teamId;
        this.title = b.title;
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

        public LinearUpdateIssuePatch build() {
            return new LinearUpdateIssuePatch(this);
        }
    }
}
