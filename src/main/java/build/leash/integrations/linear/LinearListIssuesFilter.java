package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Narrowing filter for {@code Linear.listIssues(...)}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LinearListIssuesFilter {

    @JsonProperty("teamId")
    private final String teamId;

    @JsonProperty("assigneeId")
    private final String assigneeId;

    @JsonProperty("stateType")
    private final String stateType;

    @JsonProperty("limit")
    private final Integer limit;

    @JsonProperty("cursor")
    private final String cursor;

    private LinearListIssuesFilter(Builder b) {
        this.teamId = b.teamId;
        this.assigneeId = b.assigneeId;
        this.stateType = b.stateType;
        this.limit = b.limit;
        this.cursor = b.cursor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTeamId() { return teamId; }
    public String getAssigneeId() { return assigneeId; }
    public String getStateType() { return stateType; }
    public Integer getLimit() { return limit; }
    public String getCursor() { return cursor; }

    public static final class Builder {
        private String teamId;
        private String assigneeId;
        private String stateType;
        private Integer limit;
        private String cursor;

        public Builder teamId(String teamId) { this.teamId = teamId; return this; }
        public Builder assigneeId(String assigneeId) { this.assigneeId = assigneeId; return this; }
        public Builder stateType(String stateType) { this.stateType = stateType; return this; }
        public Builder stateType(LinearStateType stateType) {
            this.stateType = stateType == null ? null : stateType.wireValue();
            return this;
        }
        public Builder limit(Integer limit) { this.limit = limit; return this; }
        public Builder cursor(String cursor) { this.cursor = cursor; return this; }

        public LinearListIssuesFilter build() {
            return new LinearListIssuesFilter(this);
        }
    }
}
