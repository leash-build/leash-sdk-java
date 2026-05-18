package build.leash.integrations.gmail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/** Response from {@code Gmail.listLabels()}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GmailLabelList {

    private final List<GmailLabel> labels;

    public GmailLabelList(@JsonProperty("labels") List<GmailLabel> labels) {
        this.labels = labels == null ? Collections.emptyList() : List.copyOf(labels);
    }

    public List<GmailLabel> getLabels() {
        return labels;
    }
}
