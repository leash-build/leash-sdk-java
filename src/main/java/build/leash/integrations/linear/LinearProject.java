package build.leash.integrations.linear;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/** A Linear project. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class LinearProject {

    private final String id;
    private final String name;
    private final String description;
    private final LinearProjectState state;
    private final String targetDate;
    private final String startDate;
    private final String url;
    private final List<String> teamIds;
    private final Double progress;

    public LinearProject(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("state") String state,
            @JsonProperty("targetDate") String targetDate,
            @JsonProperty("startDate") String startDate,
            @JsonProperty("url") String url,
            @JsonProperty("teamIds") List<String> teamIds,
            @JsonProperty("progress") Double progress) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.state = LinearProjectState.fromWire(state);
        this.targetDate = targetDate;
        this.startDate = startDate;
        this.url = url;
        this.teamIds = teamIds == null ? Collections.emptyList() : List.copyOf(teamIds);
        this.progress = progress;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LinearProjectState getState() { return state; }
    public String getTargetDate() { return targetDate; }
    public String getStartDate() { return startDate; }
    public String getUrl() { return url; }
    public List<String> getTeamIds() { return teamIds; }
    public Double getProgress() { return progress; }
}
