package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Narrowing filter for {@code Linear.listProjects(...)}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LinearListProjectsFilter {

    @JsonProperty("teamId")
    private final String teamId;

    private LinearListProjectsFilter(Builder b) {
        this.teamId = b.teamId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTeamId() { return teamId; }

    public static final class Builder {
        private String teamId;

        public Builder teamId(String teamId) { this.teamId = teamId; return this; }

        public LinearListProjectsFilter build() {
            return new LinearListProjectsFilter(this);
        }
    }
}
