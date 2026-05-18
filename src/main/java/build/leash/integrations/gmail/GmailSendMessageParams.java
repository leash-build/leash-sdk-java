package build.leash.integrations.gmail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** Body for {@code Gmail.sendMessage(...)}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class GmailSendMessageParams {

    @JsonProperty("to")
    private final String to;

    @JsonProperty("subject")
    private final String subject;

    @JsonProperty("body")
    private final String body;

    @JsonProperty("cc")
    private final String cc;

    @JsonProperty("bcc")
    private final String bcc;

    private GmailSendMessageParams(Builder b) {
        this.to = Objects.requireNonNull(b.to, "to");
        this.subject = Objects.requireNonNull(b.subject, "subject");
        this.body = Objects.requireNonNull(b.body, "body");
        this.cc = b.cc;
        this.bcc = b.bcc;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getCc() {
        return cc;
    }

    public String getBcc() {
        return bcc;
    }

    public static final class Builder {
        private String to;
        private String subject;
        private String body;
        private String cc;
        private String bcc;

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder cc(String cc) {
            this.cc = cc;
            return this;
        }

        public Builder bcc(String bcc) {
            this.bcc = bcc;
            return this;
        }

        public GmailSendMessageParams build() {
            return new GmailSendMessageParams(this);
        }
    }
}
