package build.leash.integrations;

import build.leash.Leash;
import build.leash.TestSupport;
import build.leash.integrations.linear.LinearComment;
import build.leash.integrations.linear.LinearCreateIssueInput;
import build.leash.integrations.linear.LinearIssue;
import build.leash.integrations.linear.LinearListIssuesFilter;
import build.leash.integrations.linear.LinearListIssuesResult;
import build.leash.integrations.linear.LinearListProjectsFilter;
import build.leash.integrations.linear.LinearProject;
import build.leash.integrations.linear.LinearStateType;
import build.leash.integrations.linear.LinearTeam;
import build.leash.integrations.linear.LinearUpdateIssuePatch;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinearIntegrationTest {

    private WireMockServer wm;
    private Leash leash;

    @BeforeEach
    void setUp() {
        wm = TestSupport.startWireMock();
        leash = TestSupport.leashFor(wm, "lsk_live_test");
    }

    @AfterEach
    void tearDown() {
        if (wm != null) wm.stop();
    }

    @Test
    void listIssues_envelopedResponse() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/list_issues"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"issues\":[{\"id\":\"i1\",\"title\":\"Bug\"}],\"cursor\":\"c1\"}")));

        LinearListIssuesResult res = leash.integrations().linear().listIssues();
        assertEquals(1, res.getIssues().size());
        assertEquals("Bug", res.getIssues().get(0).getTitle());
        assertEquals("c1", res.getCursor().orElseThrow());
    }

    @Test
    void listIssues_bareArrayResponse() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/list_issues"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"i1\",\"title\":\"Bug\"}]")));

        LinearListIssuesResult res = leash.integrations().linear().listIssues();
        assertEquals(1, res.getIssues().size());
        assertTrue(res.getCursor().isEmpty());
    }

    @Test
    void listIssues_withFilter() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/list_issues"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"issues\":[]}")));

        leash.integrations().linear().listIssues(
                LinearListIssuesFilter.builder()
                        .stateType(LinearStateType.STARTED)
                        .limit(20)
                        .build());

        verify(postRequestedFor(urlEqualTo("/api/integrations/linear/list_issues"))
                .withRequestBody(equalToJson("{\"stateType\":\"started\",\"limit\":20}")));
    }

    @Test
    void getIssue_sendsId() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/get_issue"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"i1\",\"title\":\"Bug\"}")));

        LinearIssue issue = leash.integrations().linear().getIssue("i1");
        assertEquals("i1", issue.getId());

        verify(postRequestedFor(urlEqualTo("/api/integrations/linear/get_issue"))
                .withRequestBody(equalToJson("{\"id\":\"i1\"}")));
    }

    @Test
    void createIssue_serialisesBody() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/create_issue"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"i2\",\"title\":\"New\"}")));

        leash.integrations().linear().createIssue(
                LinearCreateIssueInput.builder()
                        .teamId("team-1")
                        .title("New")
                        .description("body text")
                        .priority(2)
                        .build());

        verify(postRequestedFor(urlEqualTo("/api/integrations/linear/create_issue"))
                .withRequestBody(equalToJson(
                        "{\"teamId\":\"team-1\",\"title\":\"New\",\"description\":\"body text\",\"priority\":2}")));
    }

    @Test
    void updateIssue_mergesIdIntoBody() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/update_issue"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"i1\",\"title\":\"Renamed\"}")));

        leash.integrations().linear().updateIssue("i1",
                LinearUpdateIssuePatch.builder()
                        .title("Renamed")
                        .priority(1)
                        .build());

        verify(postRequestedFor(urlEqualTo("/api/integrations/linear/update_issue"))
                .withRequestBody(equalToJson(
                        "{\"id\":\"i1\",\"title\":\"Renamed\",\"priority\":1}")));
    }

    @Test
    void addComment_sendsIssueIdAndBody() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/add_comment"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"c1\",\"body\":\"ack\",\"issueId\":\"i1\"}")));

        LinearComment c = leash.integrations().linear().addComment("i1", "ack");
        assertEquals("c1", c.getId());

        verify(postRequestedFor(urlEqualTo("/api/integrations/linear/add_comment"))
                .withRequestBody(equalToJson("{\"issueId\":\"i1\",\"body\":\"ack\"}")));
    }

    @Test
    void listTeams_envelopedResponse() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/list_teams"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"teams\":[{\"id\":\"t1\",\"key\":\"LEA\",\"name\":\"Leash\"}]}")));

        List<LinearTeam> teams = leash.integrations().linear().listTeams();
        assertEquals(1, teams.size());
        assertEquals("LEA", teams.get(0).getKey());
    }

    @Test
    void listTeams_bareArrayResponse() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/list_teams"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"t1\",\"key\":\"LEA\",\"name\":\"Leash\"}]")));

        List<LinearTeam> teams = leash.integrations().linear().listTeams();
        assertEquals(1, teams.size());
    }

    @Test
    void listTeams_nullResponse() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/list_teams"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("null")));

        List<LinearTeam> teams = leash.integrations().linear().listTeams();
        assertTrue(teams.isEmpty());
    }

    @Test
    void listProjects_envelopeShape() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/list_projects"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"projects\":[{\"id\":\"p1\",\"name\":\"Q4\",\"state\":\"started\"}]}")));

        List<LinearProject> projects = leash.integrations().linear().listProjects();
        assertEquals(1, projects.size());
        assertEquals("Q4", projects.get(0).getName());
    }

    @Test
    void listProjects_withFilter() {
        wm.stubFor(post(urlEqualTo("/api/integrations/linear/list_projects"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"projects\":[]}")));

        leash.integrations().linear().listProjects(
                LinearListProjectsFilter.builder().teamId("t1").build());

        verify(postRequestedFor(urlEqualTo("/api/integrations/linear/list_projects"))
                .withRequestBody(equalToJson("{\"teamId\":\"t1\"}")));
    }
}
