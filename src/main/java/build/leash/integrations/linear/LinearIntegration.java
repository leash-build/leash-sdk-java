package build.leash.integrations.linear;

import build.leash.internal.Json;
import build.leash.internal.Transport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Typed Linear provider client. Underscored wire-action names match the
 * Linear MCP server (and the TS / Python / Go surfaces). Tolerant of the
 * platform sometimes returning a bare array vs. an enveloped object — see
 * the per-method docs.
 */
public final class LinearIntegration {

    private static final String PROVIDER = "linear";

    private final Transport transport;

    public LinearIntegration(Transport transport) {
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    public LinearListIssuesResult listIssues() {
        return listIssues(null);
    }

    /**
     * Lists issues, optionally filtered. Accepts either the enveloped
     * {@code { issues, cursor }} shape or a bare {@code [issue, ...]} array
     * from the upstream provider.
     */
    public LinearListIssuesResult listIssues(LinearListIssuesFilter filter) {
        JsonNode raw = transport.integrationsCall(PROVIDER, "list_issues", filter);
        if (raw == null || raw.isNull()) {
            return new LinearListIssuesResult(Collections.emptyList(), null);
        }
        if (raw.isArray()) {
            List<LinearIssue> issues = Json.treeToValue(raw, new TypeReference<List<LinearIssue>>() {});
            return new LinearListIssuesResult(issues == null ? Collections.emptyList() : issues, null);
        }
        return Json.treeToValue(raw, LinearListIssuesResult.class);
    }

    public LinearIssue getIssue(String id) {
        Objects.requireNonNull(id, "id");
        ObjectNode body = Json.newObject();
        body.put("id", id);
        JsonNode raw = transport.integrationsCall(PROVIDER, "get_issue", body);
        return Json.treeToValue(raw, LinearIssue.class);
    }

    public LinearIssue createIssue(LinearCreateIssueInput input) {
        Objects.requireNonNull(input, "input");
        JsonNode raw = transport.integrationsCall(PROVIDER, "create_issue", input);
        return Json.treeToValue(raw, LinearIssue.class);
    }

    /** Merges {@code id} into the patch body — wire-shape contract from the upstream MCP server. */
    public LinearIssue updateIssue(String id, LinearUpdateIssuePatch patch) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(patch, "patch");
        ObjectNode body = (ObjectNode) Json.MAPPER.valueToTree(patch);
        body.put("id", id);
        JsonNode raw = transport.integrationsCall(PROVIDER, "update_issue", body);
        return Json.treeToValue(raw, LinearIssue.class);
    }

    public LinearComment addComment(String issueId, String body) {
        Objects.requireNonNull(issueId, "issueId");
        Objects.requireNonNull(body, "body");
        ObjectNode params = Json.newObject();
        params.put("issueId", issueId);
        params.put("body", body);
        JsonNode raw = transport.integrationsCall(PROVIDER, "add_comment", params);
        return Json.treeToValue(raw, LinearComment.class);
    }

    /** Tolerates envelope {@code { teams: [...] }} and bare {@code [...]} responses. */
    public List<LinearTeam> listTeams() {
        JsonNode raw = transport.integrationsCall(PROVIDER, "list_teams", null);
        if (raw == null || raw.isNull()) return Collections.emptyList();
        if (raw.isArray()) {
            List<LinearTeam> out = Json.treeToValue(raw, new TypeReference<List<LinearTeam>>() {});
            return out == null ? Collections.emptyList() : out;
        }
        JsonNode teams = raw.get("teams");
        if (teams != null && teams.isArray()) {
            List<LinearTeam> out = Json.treeToValue(teams, new TypeReference<List<LinearTeam>>() {});
            return out == null ? Collections.emptyList() : out;
        }
        return Collections.emptyList();
    }

    public List<LinearProject> listProjects() {
        return listProjects(null);
    }

    /** Tolerates envelope {@code { projects: [...] }} and bare {@code [...]} responses. */
    public List<LinearProject> listProjects(LinearListProjectsFilter filter) {
        JsonNode raw = transport.integrationsCall(PROVIDER, "list_projects", filter);
        if (raw == null || raw.isNull()) return Collections.emptyList();
        if (raw.isArray()) {
            List<LinearProject> out = Json.treeToValue(raw, new TypeReference<List<LinearProject>>() {});
            return out == null ? Collections.emptyList() : out;
        }
        JsonNode projects = raw.get("projects");
        if (projects != null && projects.isArray()) {
            List<LinearProject> out = Json.treeToValue(projects, new TypeReference<List<LinearProject>>() {});
            return out == null ? Collections.emptyList() : out;
        }
        return Collections.emptyList();
    }
}
